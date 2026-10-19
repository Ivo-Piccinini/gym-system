package com.utnGymGroup.gym_system.features.user;

import com.utnGymGroup.gym_system.common.auth.credentials.CredentialsEntity;
import com.utnGymGroup.gym_system.common.auth.dto.NewAccountRequest;
import com.utnGymGroup.gym_system.common.auth.credentials.CredentialsRepository;
import com.utnGymGroup.gym_system.common.auth.jwt.JwtService;
import com.utnGymGroup.gym_system.common.auth.permissions.RoleEntity;
import com.utnGymGroup.gym_system.common.auth.permissions.RoleRepository;
import com.utnGymGroup.gym_system.common.auth.permissions.Roles;
import com.utnGymGroup.gym_system.common.auth.permissions.exceptions.RoleNotFoundException;
import com.utnGymGroup.gym_system.features.audit.AuditActions;
import com.utnGymGroup.gym_system.features.audit.Auditable;
import com.utnGymGroup.gym_system.features.user.dtos.*;
import com.utnGymGroup.gym_system.features.user.exceptions.*;
import com.utnGymGroup.gym_system.features.user.mappers.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final CredentialsRepository credentialsRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public List<UserDTO> findAllUsers(Boolean enabled, Roles role){
        if(role != null){
            return findAllUsersByRole(role);
        }
        if(enabled != null){
            return findAllUsersByStatus(enabled);
        }

        return credentialsRepository.findAll().stream()
                .map(this::convertToDtoWithRole)
                .toList();
    }

    public List<UserDTO> findAllUsersByStatus(Boolean enabled){
        List<CredentialsEntity> credentialsList = credentialsRepository.findAllByEnabled(enabled);
        return credentialsList.stream()
                .map(this::convertToDtoWithRole)
                .toList();
    }

    public UserDTO findByUsername(String username){
        return credentialsRepository.findByUsername(username)
                .map(this::convertToDtoWithRole)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado. USERNAME: " + username));
    }

    public UserDTO findByEmail(String email){
        UserEntity userEntity = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado. EMAIL: " + email));
        return convertToDtoWithRole(userEntity);
    }

    @Transactional
    @Auditable(AuditActions.USER_REGISTRATION)
    public UserDTO userRegister(UserDTO request){
        if(userRepository.existsByEmail(request.getEmail())){
            throw new UserAlreadyExistsException("Ya existe un usuario con este email.");
        }
        // definimos que el username por defecto va a ser el dni, luego tendrá la posibilidad de cambiarlo.
        String defaultUsername = request.getDni();
        if(credentialsRepository.findByUsername(defaultUsername).isPresent()){
            throw new UserAlreadyExistsException("El usuario ya existe.");
        }

        UserEntity userEntity = userMapper.convertToEntity(request);

        UserEntity savedUser = userRepository.save(userEntity);

        Roles targetRole = request.getRole() != null ? request.getRole() : Roles.ROLE_CLIENT;
        RoleEntity roleEntity = roleRepository.findByRole(targetRole)
                .orElseThrow(() -> new RoleNotFoundException("Rol no encontrado. ROL: " + targetRole));

        CredentialsEntity credentials = CredentialsEntity.builder()
                .username(defaultUsername)
                .password(passwordEncoder.encode(request.getDni())) // <-- Cifrado de contraseña en vivo
                .enabled(true)
                .user(savedUser) // Vinculación 1 a 1 con UserEntity
                .build();

        credentials.getRoles().add(roleEntity); // Asignar Rol
        credentialsRepository.save(credentials);

        UserDTO responseDTO = userMapper.convertToDto(savedUser);
        responseDTO.setRole(targetRole);

        return responseDTO;
    }

    @Transactional
    @Auditable(AuditActions.USER_REGISTRATION)
    public UserDTO userRegister(NewAccountRequest request){
        if(userRepository.existsByEmail(request.email())){
            throw new UserAlreadyExistsException("Ya existe un usuario con este email.");
        }
        if(credentialsRepository.existsByUsername(request.username())){
            throw new UserAlreadyExistsException("Nombre de usuario no disponible.");
        }

        UserEntity userEntity = UserEntity.builder()
                .email(request.email())
                .dni(request.dni())
                .build();

        UserEntity savedUser = userRepository.save(userEntity);

        RoleEntity roleClient = roleRepository.findByRole(Roles.ROLE_CLIENT)
                .orElseThrow(() -> new RoleNotFoundException("Error: Rol ROLE_CLIENT no encontrado en la base de datos."));

        CredentialsEntity credentials = CredentialsEntity.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .enabled(true)
                .user(savedUser)
                .build();

        credentials.getRoles().add(roleClient);
        credentialsRepository.save(credentials);
        
        UserDTO responseDTO = userMapper.convertToDto(savedUser);
        responseDTO.setRole(Roles.ROLE_CLIENT);
        return responseDTO;
    }

    @Transactional
    @Auditable(AuditActions.CHANGE_PASSWORD)
    public void changePassword(String username, PasswordChangeDTO request){
        if(!request.getConfirmPassword().equals(request.getNewPassword())){
            throw new InvalidCurrentPasswordException("La nueva contraseña y la confirmación no coinciden");
        }

        CredentialsEntity credentials = credentialsRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado. USERNAME: " + username));

        if(!passwordEncoder.matches(request.getOldPassword(), credentials.getPassword())){
            throw new InvalidCurrentPasswordException("La contraseña actual es incorrecta.");
        }

        if(request.getNewPassword().equals(request.getOldPassword())){
            throw new InvalidCurrentPasswordException("La nueva contraseña no puede ser igual a la actual.");
        }

        String encryptedPassword = passwordEncoder.encode(request.getNewPassword());
        credentials.setPassword(encryptedPassword);
        credentialsRepository.save(credentials);
    }

    @Transactional
    public void changeUsername(String currentUsername, String newUsername){
        CredentialsEntity credentials = credentialsRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado. USERNAME: " + currentUsername));

        String dni = credentials.getUser().getDni();

        // esta logica es para que solo puedan cambiar de username los que fueron registrados por los admins (ya que tienen de username el dni)
        if(!credentials.getUsername().equals(dni)){
            throw new IllegalStateException("No tienes permitido modificar tu nombre de usuario.");
        }

        if(credentialsRepository.findByUsername(newUsername).isPresent()){
            throw new UserAlreadyExistsException("El username '" + newUsername + "' ya está en uso.");
        }

        credentials.setUsername(newUsername);
        credentialsRepository.save(credentials);
    }

    @Transactional
    @Auditable(AuditActions.UPDATE_PROFILE)
    public UserDTO updateUser(String username, UserDTO userDTO){
        CredentialsEntity credentials = credentialsRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado. USERNAME: " + username));
        UserEntity user = credentials.getUser();
        userMapper.updateEntityFromDTO(userDTO, user);

        UserEntity savedUser = userRepository.save(user);
        return convertToDtoWithRole(credentials);
    }

    // Función de baja lógica / reactivación de cuenta
    @Auditable(AuditActions.TOGGLE_USER_STATUS)
    @Transactional
    public void toggleUserStatus(UUID publicId, boolean enabled){
        CredentialsEntity user = credentialsRepository.findByUser_PublicId(publicId)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado. PUBLICID: " + publicId));

        user.setEnabled(enabled);
        credentialsRepository.save(user);
    }

    @Auditable(AuditActions.DELETE_USER)
    @Transactional
    public void deleteUser(String username){
        CredentialsEntity user = credentialsRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado. USERNAME: " + username));

        user.setEnabled(false);
        credentialsRepository.save(user);
    }

    @Transactional
    @Auditable(AuditActions.UPDATE_PROFILE)
    public UserDTO updatePartialUser(UserPatchDTO userPatchDTO){
        String username = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();

        CredentialsEntity credentials = credentialsRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado. USERNAME: " + username));
        UserEntity user = credentials.getUser();
        if (userPatchDTO.getFirstName() != null) {
            user.setFirstName(userPatchDTO.getFirstName());
        }
        if (userPatchDTO.getLastName() != null) {
            user.setLastName(userPatchDTO.getLastName());
        }
        if (userPatchDTO.getPhone() != null) {
            user.setPhone(userPatchDTO.getPhone());
        }
        if (userPatchDTO.getBirthDay() != null) {
            user.setBirthDate(userPatchDTO.getBirthDay());
        }

        UserEntity savedUser = userRepository.save(user);
        return convertToDtoWithRole(credentials);
    }

    public List<UserDTO> findAllUsersByRole(Roles role){
        List<CredentialsEntity> credentialsList = credentialsRepository.findAllByRole(role);

        return credentialsList.stream()
                .map(this::convertToDtoWithRole)
                .toList();
    }

    public UserEntity getAuthenticatedUserEntity() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return credentialsRepository.findByUsername(username)
                .map(CredentialsEntity::getUser)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado. USERNAME: " + username));
    }

    private UserDTO convertToDtoWithRole(CredentialsEntity credentials) {
        UserDTO dto = userMapper.convertToDto(credentials.getUser());
        if (credentials.getRoles() != null && !credentials.getRoles().isEmpty()) {
            credentials.getRoles().stream()
                    .findFirst()
                    .ifPresent(roleEntity -> dto.setRole(roleEntity.getRole()));
        }
        return dto;
    }

    private UserDTO convertToDtoWithRole(UserEntity userEntity) {
        UserDTO dto = userMapper.convertToDto(userEntity);
        if (userEntity.getPublicId() != null) {
            credentialsRepository.findByUser_PublicId(userEntity.getPublicId())
                    .ifPresent(credentials -> {
                        if (credentials.getRoles() != null && !credentials.getRoles().isEmpty()) {
                            credentials.getRoles().stream()
                                    .findFirst()
                                    .ifPresent(roleEntity -> dto.setRole(roleEntity.getRole()));
                        }
                    });
        }
        return dto;
    }

}
