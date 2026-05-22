package com.utnGymGroup.gym_system.features.user.mappers;

import com.utnGymGroup.gym_system.common.interfaces.IMapper;
import com.utnGymGroup.gym_system.features.user.UserEntity;
import com.utnGymGroup.gym_system.features.user.dtos.UserResponseDTO;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class UserResponseMapper implements IMapper<UserEntity, UserResponseDTO> {
    private final ModelMapper modelMapper;

    public UserResponseMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @Override
    public UserResponseDTO convertToDto(UserEntity userEntity) {
        return modelMapper.map(userEntity, UserResponseDTO.class);
    }

    @Override
    public UserEntity convertToEntity(UserResponseDTO userResponseDTO) {
        return modelMapper.map(userResponseDTO, UserEntity.class);
    }

    @Override
    public void updateEntityFromDTO(UserResponseDTO userResponseDTO, UserEntity userEntity) {
        modelMapper.map(userResponseDTO, userEntity);
    }
}
