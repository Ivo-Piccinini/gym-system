package com.utnGymGroup.gym_system.features.user;

import com.utnGymGroup.gym_system.common.auth.credentials.CredentialsEntity;
import com.utnGymGroup.gym_system.common.auth.credentials.CredentialsRepository;
import com.utnGymGroup.gym_system.common.auth.dto.AuthResponse;
import com.utnGymGroup.gym_system.common.auth.jwt.JwtService;
import com.utnGymGroup.gym_system.common.auth.permissions.RoleEntity;
import com.utnGymGroup.gym_system.common.auth.permissions.RoleRepository;
import com.utnGymGroup.gym_system.common.auth.permissions.Roles;
import com.utnGymGroup.gym_system.features.user.dtos.PasswordChangeDTO;
import com.utnGymGroup.gym_system.features.user.dtos.UserDTO;
import com.utnGymGroup.gym_system.features.user.dtos.UserPatchDTO;
import com.utnGymGroup.gym_system.features.user.exceptions.UserAlreadyExistsException;
import com.utnGymGroup.gym_system.features.user.mappers.UserMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private CredentialsRepository credentialsRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private com.utnGymGroup.gym_system.features.subscription.SubscriptionRepository subscriptionRepository;

    @InjectMocks
    private UserService userService;

    private final String testUsername = "cliente_prueba";
    private UserEntity testUser;
    private CredentialsEntity testCredentials;

    @BeforeEach
    void setUp() {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(testUsername, "password");
        SecurityContextHolder.getContext().setAuthentication(auth);

        testUser = UserEntity.builder()
                .publicId(UUID.randomUUID())
                .firstName("Juan")
                .lastName("Perez")
                .email("juan@test.com")
                .dni("12345678")
                .phone("1122334455")
                .birthDate(LocalDate.of(1995, 5, 20))
                .build();

        RoleEntity clientRole = RoleEntity.builder()
                .role(Roles.ROLE_CLIENT)
                .build();

        testCredentials = CredentialsEntity.builder()
                .username(testUsername)
                .password("encoded_pass")
                .user(testUser)
                .roles(Set.of(clientRole))
                .enabled(true)
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("updatePartialUser actualiza teléfono y fecha de nacimiento del usuario autenticado")
    void shouldUpdatePartialUserSuccessfully() {
        // Arrange
        LocalDate newBirthDate = LocalDate.of(1998, 12, 10);
        String newPhone = "3419998877";

        UserPatchDTO patchDTO = UserPatchDTO.builder()
                .phone(newPhone)
                .birthDay(newBirthDate)
                .build();

        when(credentialsRepository.findByUsername(testUsername)).thenReturn(Optional.of(testCredentials));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDTO mappedDto = new UserDTO();
        mappedDto.setFirstName("Juan");
        mappedDto.setLastName("Perez");
        mappedDto.setPhone(newPhone);
        mappedDto.setBirthDate(newBirthDate);
        when(userMapper.convertToDto(any(UserEntity.class))).thenReturn(mappedDto);

        // Act
        UserDTO result = userService.updatePartialUser(patchDTO);

        // Assert
        assertNotNull(result);
        assertEquals(newPhone, testUser.getPhone());
        assertEquals(newBirthDate, testUser.getBirthDate());
        assertEquals(Roles.ROLE_CLIENT, result.getRole());
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    @DisplayName("findByUsername devuelve el UserDTO del usuario solicitado con su rol")
    void shouldFindUserByUsernameWithRole() {
        // Arrange
        when(credentialsRepository.findByUsername(testUsername)).thenReturn(Optional.of(testCredentials));

        UserDTO mappedDto = new UserDTO();
        mappedDto.setFirstName("Juan");
        mappedDto.setLastName("Perez");
        mappedDto.setEmail("juan@test.com");
        mappedDto.setDni("12345678");
        when(userMapper.convertToDto(testUser)).thenReturn(mappedDto);

        // Act
        UserDTO result = userService.findByUsername(testUsername);

        // Assert
        assertNotNull(result);
        assertEquals("Juan", result.getFirstName());
        assertEquals("Perez", result.getLastName());
        assertEquals(Roles.ROLE_CLIENT, result.getRole());
        assertEquals(testUsername, result.getUsername());
        assertTrue(result.getEnabled());
    }

    @Test
    @DisplayName("findAllUsers devuelve la lista de usuarios con username y estado enabled")
    void shouldFindAllUsersWithUsernameAndStatus() {
        // Arrange
        when(credentialsRepository.findAll()).thenReturn(java.util.List.of(testCredentials));

        UserDTO mappedDto = new UserDTO();
        mappedDto.setFirstName("Juan");
        mappedDto.setLastName("Perez");
        when(userMapper.convertToDto(testUser)).thenReturn(mappedDto);

        // Act
        java.util.List<UserDTO> results = userService.findAllUsers(null, null);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        UserDTO dto = results.getFirst();
        assertEquals(testUsername, dto.getUsername());
        assertTrue(dto.getEnabled());
        assertEquals(Roles.ROLE_CLIENT, dto.getRole());
    }

    @Test
    @DisplayName("changePassword actualiza la contraseña exitosamente si las validaciones pasan")
    void shouldChangePasswordSuccessfully() {
        PasswordChangeDTO request = new PasswordChangeDTO("encoded_pass", "newPassword123", "newPassword123");
        when(credentialsRepository.findByUsername(testUsername)).thenReturn(Optional.of(testCredentials));
        when(passwordEncoder.matches("encoded_pass", "encoded_pass")).thenReturn(true);
        when(passwordEncoder.encode("newPassword123")).thenReturn("new_encoded_pass");

        userService.changePassword(testUsername, request);

        assertEquals("new_encoded_pass", testCredentials.getPassword());
        verify(credentialsRepository, times(1)).save(testCredentials);
    }

    @Test
    @DisplayName("changeUsername permite personalizar username si el actual es su DNI y genera nuevo token")
    void shouldChangeUsernameSuccessfullyWhenUsernameIsDni() {
        testCredentials.setUsername("12345678");
        when(credentialsRepository.findByUsername("12345678")).thenReturn(Optional.of(testCredentials));
        when(credentialsRepository.findByUsername("juan_nuevo")).thenReturn(Optional.empty());
        when(jwtService.generateToken(testCredentials)).thenReturn("new_mocked_jwt");

        AuthResponse response = userService.changeUsername("12345678", "juan_nuevo");

        assertNotNull(response);
        assertEquals("new_mocked_jwt", response.token());
        assertEquals("juan_nuevo", testCredentials.getUsername());
        assertFalse(response.requiresPasswordChange());
        verify(credentialsRepository, times(1)).save(testCredentials);
    }

    @Test
    @DisplayName("userRegister lanza UserAlreadyExistsException con mensaje amigable cuando el DNI ya existe")
    void shouldThrowExceptionWhenRegisteringUserWithExistingDni() {
        UserDTO dto = new UserDTO();
        dto.setDni("12345678");
        dto.setEmail("nuevo@gym.com");

        when(userRepository.existsByDni("12345678")).thenReturn(true);

        UserAlreadyExistsException exception = assertThrows(UserAlreadyExistsException.class, () -> {
            userService.userRegister(dto);
        });

        assertEquals("Ya existe un usuario registrado con el DNI ingresado.", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("userRegister lanza UserAlreadyExistsException con mensaje amigable cuando el Email ya existe")
    void shouldThrowExceptionWhenRegisteringUserWithExistingEmail() {
        UserDTO dto = new UserDTO();
        dto.setDni("87654321");
        dto.setEmail("juan@test.com");

        when(userRepository.existsByDni("87654321")).thenReturn(false);
        when(userRepository.existsByEmail("juan@test.com")).thenReturn(true);

        UserAlreadyExistsException exception = assertThrows(UserAlreadyExistsException.class, () -> {
            userService.userRegister(dto);
        });

        assertEquals("Ya existe un usuario con este email.", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("userRegister lanza IllegalArgumentException si el usuario es menor de 18 años")
    void shouldThrowExceptionWhenRegisteringUserUnder18() {
        UserDTO dto = new UserDTO();
        dto.setDni("88887777");
        dto.setEmail("menor@gym.com");
        dto.setBirthDate(LocalDate.now().minusYears(17));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.userRegister(dto);
        });

        assertEquals("El usuario debe tener al menos 18 años de edad.", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("updatePartialUser lanza IllegalArgumentException si la nueva fecha de nacimiento corresponde a un menor de 18 años")
    void shouldThrowExceptionWhenUpdatingPartialUserUnder18() {
        UserPatchDTO patchDTO = UserPatchDTO.builder()
                .birthDay(LocalDate.now().minusYears(16))
                .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.updatePartialUser(patchDTO);
        });

        assertEquals("El usuario debe tener al menos 18 años de edad.", exception.getMessage());
        verify(userRepository, never()).save(any());
    }
}
