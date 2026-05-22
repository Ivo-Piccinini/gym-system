package com.utnGymGroup.gym_system.features.user.mappers;

import com.utnGymGroup.gym_system.common.interfaces.IMapper;
import com.utnGymGroup.gym_system.features.user.UserEntity;
import com.utnGymGroup.gym_system.features.user.dtos.UserCreateRequestDTO;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class UserCreateRequestMapper implements IMapper<UserEntity, UserCreateRequestDTO> {
    private final ModelMapper modelMapper;

    public UserCreateRequestMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @Override
    public UserCreateRequestDTO convertToDto(UserEntity userEntity) {
        return modelMapper.map(userEntity, UserCreateRequestDTO.class);
    }

    @Override
    public UserEntity convertToEntity(UserCreateRequestDTO userCreateRequestDTO) {
        return modelMapper.map(userCreateRequestDTO, UserEntity.class);
    }

    @Override
    public void updateEntityFromDTO(UserCreateRequestDTO userCreateRequestDTO, UserEntity userEntity) {
        modelMapper.map(userCreateRequestDTO, userEntity);
    }
}
