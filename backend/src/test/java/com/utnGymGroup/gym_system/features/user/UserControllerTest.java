package com.utnGymGroup.gym_system.features.user;

import com.utnGymGroup.gym_system.common.auth.permissions.Roles;
import com.utnGymGroup.gym_system.features.user.dtos.UserDTO;
import com.utnGymGroup.gym_system.features.user.dtos.UserPatchDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private final String authUsername = "cliente_auth";

    @BeforeEach
    void setUp() {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(authUsername, "pass");
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("GET /api/users/me devuelve el perfil del usuario autenticado")
    void shouldReturnLoggedUserProfile() {
        // Arrange
        UserDTO userDTO = new UserDTO();
        userDTO.setPublicId(UUID.randomUUID());
        userDTO.setFirstName("Ana");
        userDTO.setLastName("Gomez");
        userDTO.setEmail("ana@gym.com");
        userDTO.setDni("87654321");
        userDTO.setPhone("3415551234");
        userDTO.setBirthDate(LocalDate.of(1996, 4, 15));
        userDTO.setRole(Roles.ROLE_CLIENT);

        when(userService.findByUsername(authUsername)).thenReturn(userDTO);

        // Act
        ResponseEntity<UserDTO> response = userController.getLoggedUser();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Ana", response.getBody().getFirstName());
        assertEquals("87654321", response.getBody().getDni());
        verify(userService, times(1)).findByUsername(authUsername);
    }

    @Test
    @DisplayName("PATCH /api/users/me actualiza parcialmente los datos personales del usuario logueado")
    void shouldUpdatePartialLoggedUser() {
        // Arrange
        UserPatchDTO patchDTO = UserPatchDTO.builder()
                .phone("3417778899")
                .birthDay(LocalDate.of(1997, 7, 20))
                .build();

        UserDTO updatedUser = new UserDTO();
        updatedUser.setPhone("3417778899");
        updatedUser.setBirthDate(LocalDate.of(1997, 7, 20));
        updatedUser.setRole(Roles.ROLE_CLIENT);

        when(userService.updatePartialUser(patchDTO)).thenReturn(updatedUser);

        // Act
        ResponseEntity<UserDTO> response = userController.updateLoggedUser(patchDTO);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("3417778899", response.getBody().getPhone());
        assertEquals(LocalDate.of(1997, 7, 20), response.getBody().getBirthDate());
        verify(userService, times(1)).updatePartialUser(patchDTO);
    }
}
