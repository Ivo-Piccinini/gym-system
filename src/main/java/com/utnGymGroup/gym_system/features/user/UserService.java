package com.utnGymGroup.gym_system.features.user;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    public List<UserDTO> findAll(){
        return userRepository.findAll().stream()
                .map(userMapper::convertToDto)
                .toList();
    }

    public UserDTO findByUsername(String username){
        userRepository.findByUsername(username)
                .map(userMapper::convertToDto);
    }
}
