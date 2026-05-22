package com.utnGymGroup.gym_system.features.user;

import com.utnGymGroup.gym_system.features.user.dtos.UserResponseDTO;
import com.utnGymGroup.gym_system.features.user.exceptions.UserNotFoundException;
import com.utnGymGroup.gym_system.features.user.mappers.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final AuthResponseMapper authResponseMapper;
    private final LoginRequestMapper loginRequestMapper;
    private final PasswordChangeMapper passwordChangeMapper;
    private final UserCreateRequestMapper userCreateRequestMapper;
    private final UserResponseMapper userResponseMapper;
    private final UserUpdateMapper userUpdateMapper;

    public UserService(
            UserRepository userRepository,
            AuthResponseMapper authResponseMapper,
            LoginRequestMapper loginRequestMapper,
            PasswordChangeMapper passwordChangeMapper,
            UserCreateRequestMapper userCreateRequestMapper,
            UserResponseMapper userResponseMapper,
            UserUpdateMapper userUpdateMapper
    ) {
        this.userRepository = userRepository;
        this.authResponseMapper = authResponseMapper;
        this.loginRequestMapper = loginRequestMapper;
        this.passwordChangeMapper = passwordChangeMapper;
        this.userCreateRequestMapper = userCreateRequestMapper;
        this.userResponseMapper = userResponseMapper;
        this.userUpdateMapper = userUpdateMapper;
    }

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
}
