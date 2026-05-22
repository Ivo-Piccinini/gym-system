package com.utnGymGroup.gym_system.features.user.mappers;

import com.utnGymGroup.gym_system.common.interfaces.IMapper;
import com.utnGymGroup.gym_system.features.user.UserEntity;
import com.utnGymGroup.gym_system.features.user.dtos.AuthResponseDTO;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class AuthResponseMapper implements IMapper<UserEntity, AuthResponseDTO> {
    private final ModelMapper modelMapper;

    public AuthResponseMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @Override
    public AuthResponseDTO convertToDto(UserEntity userEntity) {
        return modelMapper.map(userEntity, AuthResponseDTO.class);
    }

    @Override
    public UserEntity convertToEntity(AuthResponseDTO authResponseDTO) {
        return modelMapper.map(authResponseDTO, UserEntity.class);
    }

    @Override
    public void updateEntityFromDTO(AuthResponseDTO authResponseDTO, UserEntity userEntity) {
        modelMapper.map(authResponseDTO, userEntity);
    }
}
