package com.utnGymGroup.gym_system.features.user.mappers;

import com.utnGymGroup.gym_system.common.interfaces.IMapper;
import com.utnGymGroup.gym_system.features.user.UserEntity;
import com.utnGymGroup.gym_system.features.user.dtos.LoginRequestDTO;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class LoginRequestMapper implements IMapper<UserEntity, LoginRequestDTO> {
    private final ModelMapper modelMapper;

    public LoginRequestMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @Override
    public LoginRequestDTO convertToDto(UserEntity userEntity) {
        return modelMapper.map(userEntity, LoginRequestDTO.class);
    }

    @Override
    public UserEntity convertToEntity(LoginRequestDTO loginRequestDTO) {
        return modelMapper.map(loginRequestDTO, UserEntity.class);
    }

    @Override
    public void updateEntityFromDTO(LoginRequestDTO loginRequestDTO, UserEntity userEntity) {
        modelMapper.map(loginRequestDTO,userEntity);
    }
}
