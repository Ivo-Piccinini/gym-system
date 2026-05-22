package com.utnGymGroup.gym_system.features.user.mappers;

import com.utnGymGroup.gym_system.common.interfaces.IMapper;
import com.utnGymGroup.gym_system.features.user.UserEntity;
import com.utnGymGroup.gym_system.features.user.dtos.UserUpdateDTO;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class UserUpdateMapper implements IMapper<UserEntity, UserUpdateDTO> {
    private final ModelMapper modelMapper;

    public UserUpdateMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @Override
    public UserUpdateDTO convertToDto(UserEntity userEntity) {
        return modelMapper.map(userEntity, UserUpdateDTO.class);
    }

    @Override
    public UserEntity convertToEntity(UserUpdateDTO userUpdateDTO) {
        return modelMapper.map(userUpdateDTO, UserEntity.class);
    }

    @Override
    public void updateEntityFromDTO(UserUpdateDTO userUpdateDTO, UserEntity userEntity) {
        modelMapper.map(userUpdateDTO, userEntity);
    }
}
