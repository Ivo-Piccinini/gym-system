package com.utnGymGroup.gym_system.features.user;

import com.utnGymGroup.gym_system.features.audit.AuditActions;
import com.utnGymGroup.gym_system.features.audit.Auditable;
import com.utnGymGroup.gym_system.features.user.dtos.*;
import com.utnGymGroup.gym_system.features.user.exceptions.*;
import com.utnGymGroup.gym_system.features.user.mappers.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

    public List<UserResponseDTO> findAllUsers(){
        return userRepository.findAll().stream()
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

        userEntity.setRole(Roles.CLIENT);
        userEntity.setEnabled(true);

        // TODO: En esta parte hay que hacer la encriptación de la contraseña cuando vea spring security
        userEntity.setPassword(request.getPassword());

        if(userEntity.getProfile() != null){
            userEntity.getProfile().setUser(userEntity);
        }

        UserEntity savedUser = userRepository.save(userEntity);

        return userResponseMapper.convertToDto(savedUser);
    }

    @Auditable(AuditActions.LOGIN)
    public AuthResponseDTO login(LoginRequestDTO request){
        UserEntity user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new InvalidCredentialsException("Nombre de usuario o contraseña incorrectos."));

        if(!user.getEnabled()){
            throw new UserInactiveException("Tu cuenta está desactivada, contacta a un administrador para reactivarla");
        }

        //TODO: aca iria .matches de spring security para la contraseña, por ahora lo hago con equals
        if(!user.getPassword().equals(request.getPassword())){
            throw new InvalidCredentialsException("Nombre de usuario o contraseña incorrectos.");
        }

        AuthResponseDTO responseDTO = authResponseMapper.convertToDto(user);

        // TODO: aca va a ir la generación del token con spring security
        String mockToken = "mock-jwt-token-for-" + user.getUsername() + "-12345";
        responseDTO.setToken(mockToken);

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

        // TODO: utilizar .matches cuando integremos spring security
        if(!user.getPassword().equals(request.getOldPassword())){
            throw new InvalidCurrentPasswordException("La contraseña actual es incorrecta.");
        }

        if(request.getNewPassword().equals(request.getOldPassword())){
            throw new InvalidCurrentPasswordException("La nueva contraseña no puede ser igual a la actual.");
        }

        //TODO: encriptar contraseña
        user.setPassword(request.getNewPassword());

        userRepository.save(user);
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
    public void toggleUserStatus(String username, boolean enabled){
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado. USERNAME: " + username));

        user.setEnabled(enabled);
        userRepository.save(user);
    }
}
