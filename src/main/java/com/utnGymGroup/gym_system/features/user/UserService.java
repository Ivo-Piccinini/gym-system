package com.utnGymGroup.gym_system.features.user;

import com.utnGymGroup.gym_system.common.auth.credentials.CredentialsEntity;
import com.utnGymGroup.gym_system.common.auth.dto.NewAccountRequest;
import com.utnGymGroup.gym_system.common.auth.credentials.CredentialsRepository;
import com.utnGymGroup.gym_system.common.auth.jwt.JwtService;
import com.utnGymGroup.gym_system.common.auth.permissions.RoleEntity;
import com.utnGymGroup.gym_system.common.auth.permissions.RoleRepository;
import com.utnGymGroup.gym_system.common.auth.permissions.Roles;
import com.utnGymGroup.gym_system.features.audit.AuditActions;
import com.utnGymGroup.gym_system.features.audit.Auditable;
import com.utnGymGroup.gym_system.features.profile.ProfileDTO;
import com.utnGymGroup.gym_system.features.profile.ProfileEntity;
import com.utnGymGroup.gym_system.features.user.dtos.*;
import com.utnGymGroup.gym_system.features.user.exceptions.*;
import com.utnGymGroup.gym_system.features.user.mappers.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final AuthResponseMapper authResponseMapper;
    private final LoginRequestMapper loginRequestMapper;
    private final PasswordChangeMapper passwordChangeMapper;
    private final UserCreateRequestMapper userCreateRequestMapper;
    private final UserResponseMapper userResponseMapper;
    private final UserUpdateMapper userUpdateMapper;
    private final CredentialsRepository credentialsRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public List<UserResponseDTO> findAllUsers(){
        return userRepository.findAll().stream()
                .map(userResponseMapper::convertToDto)
                .toList();
    }

    public List<UserResponseDTO> findAllUsersByStatus(Boolean enabled){
        List<UserEntity> users = userRepository.findAllByEnabled(enabled);
        return users.stream()
                .map(userResponseMapper::convertToDto)
                .toList();
    }

    public UserResponseDTO findByUsername(String username){
        return userRepository.findByUsername(username)
                .map(userResponseMapper::convertToDto)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado. USERNAME: " + username));
    }

    public UserResponseDTO findByEmail(String email){
        return userRepository.findByEmail(email)
                .map(userResponseMapper::convertToDto)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado. EMAIL: " + email));
    }

    @Transactional
    @Auditable(AuditActions.USER_REGISTRATION)
    public UserResponseDTO userRegister(UserCreateRequestDTO request){
        if(userRepository.existsByEmail(request.getEmail())){
            throw new UserAlreadyExistsException("Ya existe un usuario con este email.");
        }
        if(userRepository.existsByUsername(request.getUsername())){
            throw new UserAlreadyExistsException("Nombre de usuario no disponible.");
        }

        UserEntity userEntity = userCreateRequestMapper.convertToEntity(request);
        userEntity.setEnabled(true);
        if(userEntity.getProfile() != null){
            userEntity.getProfile().setUser(userEntity);
        }

        UserEntity savedUser = userRepository.save(userEntity);

        RoleEntity roleClient = roleRepository.findByRole(Roles.ROLE_CLIENT)
                .orElseThrow(() -> new RuntimeException("Error: Rol ROLE_CLIENT no encontrado en la base de datos."));

        CredentialsEntity credentials = CredentialsEntity.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword())) // <-- Cifrado de contraseña en vivo
                .enabled(true)
                .user(savedUser) // Vinculación 1 a 1 con UserEntity
                .build();

        credentials.getRoles().add(roleClient); // Asignar Rol
        credentialsRepository.save(credentials);
        return userResponseMapper.convertToDto(savedUser);
    }

    @Transactional
    @Auditable(AuditActions.USER_REGISTRATION)
    public UserResponseDTO userRegister(NewAccountRequest request){
        if(userRepository.existsByEmail(request.email())){
            throw new UserAlreadyExistsException("Ya existe un usuario con este email.");
        }
        if(userRepository.existsByUsername(request.username())){
            throw new UserAlreadyExistsException("Nombre de usuario no disponible.");
        }

        UserEntity userEntity = UserEntity.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .email(request.email())
                .enabled(true)
                .role(Roles.ROLE_CLIENT)
                .build();

        UserEntity savedUser = userRepository.save(userEntity);

        RoleEntity roleClient = roleRepository.findByRole(Roles.ROLE_CLIENT)
                .orElseThrow(() -> new RuntimeException("Error: Rol ROLE_CLIENT no encontrado en la base de datos."));

        CredentialsEntity credentials = CredentialsEntity.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .enabled(true)
                .user(savedUser)
                .build();

        credentials.getRoles().add(roleClient);
        credentialsRepository.save(credentials);
        return userResponseMapper.convertToDto(savedUser);
    }

    @Auditable(AuditActions.LOGIN)
    public AuthResponseDTO login(LoginRequestDTO request) {
        // 1. Buscar las credenciales en la base de datos
        CredentialsEntity credentials = credentialsRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new InvalidCredentialsException("Nombre de usuario o contraseña incorrectos."));

        if (!credentials.getEnabled()) {
            throw new UserInactiveException("Tu cuenta está desactivada, contacta a un administrador para reactivarla");
        }

        // 2. Validar contraseña usando matches de PasswordEncoder
        if (!passwordEncoder.matches(request.getPassword(), credentials.getPassword())) {
            throw new InvalidCredentialsException("Nombre de usuario o contraseña incorrectos.");
        }

        // 3. Generar el Token JWT real
        String token = jwtService.generateToken(credentials);

        AuthResponseDTO responseDTO = authResponseMapper.convertToDto(credentials.getUser());
        responseDTO.setToken(token);

        return responseDTO;
    }


    @Transactional
    @Auditable(AuditActions.CHANGE_PASSWORD)
    public void changePassword(String username, PasswordChangeDTO request){
        if(!request.getConfirmPassword().equals(request.getNewPassword())){
            throw new InvalidCurrentPasswordException("La nueva contraseña y la confirmación no coinciden");
        }

        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado. USERNAME: " + username));

        if(!passwordEncoder.matches(request.getOldPassword(), user.getPassword())){
            throw new InvalidCurrentPasswordException("La contraseña actual es incorrecta.");
        }

        if(request.getNewPassword().equals(request.getOldPassword())){
            throw new InvalidCurrentPasswordException("La nueva contraseña no puede ser igual a la actual.");
        }

        String encryptedPassword = passwordEncoder.encode(request.getNewPassword());
        user.setPassword(encryptedPassword);
        userRepository.save(user);

        credentialsRepository.findByUsername(username).ifPresent(creds -> {
            creds.setPassword(encryptedPassword);
            credentialsRepository.save(creds);
        });
    }

    @Transactional
    @Auditable(AuditActions.UPDATE_PROFILE)
    public UserResponseDTO updateUser(String username, UserUpdateDTO updateDTO){
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado. USERNAME: " + username));
        userUpdateMapper.updateEntityFromDTO(updateDTO, user);

        UserEntity savedUser = userRepository.save(user);
        return userResponseMapper.convertToDto(savedUser);
    }

    // Función de baja lógica / reactivación de cuenta
    @Auditable(AuditActions.TOGGLE_USER_STATUS)
    @Transactional
    public void toggleUserStatus(String username, boolean enabled){
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado. USERNAME: " + username));

        user.setEnabled(enabled);
        userRepository.save(user);

        credentialsRepository.findByUsername(username).ifPresent(creds -> {
            creds.setEnabled(enabled);
            credentialsRepository.save(creds);
        });
    }

    @Transactional
    public UserResponseDTO updateUserProfile(String username, UserUpdateDTO updateDTO){
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado. USERNAME: " + username));

        if(updateDTO.getProfile() != null && user.getProfile() != null){
            ProfileEntity profile = user.getProfile();
            ProfileDTO profileDTO = updateDTO.getProfile();

            profile.setFirstName(profileDTO.getFirstName());
            profile.setFirstName(profileDTO.getLastName());
            profile.setPhone(profileDTO.getPhone());
            profile.setBirthDate(profileDTO.getBirthDate());
            profile.setDni(profileDTO.getDni());
        }

        UserEntity updatedUser = userRepository.save(user);
        return userResponseMapper.convertToDto(updatedUser);
    }

}
