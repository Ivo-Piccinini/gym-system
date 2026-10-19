package com.utnGymGroup.gym_system.features.user.mappers;

import com.utnGymGroup.gym_system.common.interfaces.IMapper;
import com.utnGymGroup.gym_system.features.user.UserEntity;
import com.utnGymGroup.gym_system.features.user.dtos.PasswordChangeDTO;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class PasswordChangeMapper implements IMapper<UserEntity, PasswordChangeDTO> {
    private final ModelMapper modelMapper;

    public PasswordChangeMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @Override
    public PasswordChangeDTO convertToDto(UserEntity userEntity) {
        return modelMapper.map(userEntity, PasswordChangeDTO.class);
    }

    @Override
    public UserEntity convertToEntity(PasswordChangeDTO passwordChangeDTO) {
        return modelMapper.map(passwordChangeDTO, UserEntity.class);
    }

    @Override
    public void updateEntityFromDTO(PasswordChangeDTO passwordChangeDTO, UserEntity userEntity) {
        modelMapper.map(passwordChangeDTO, userEntity);
    }
}
