package com.utnGymGroup.gym_system.features.profile;

import com.utnGymGroup.gym_system.common.interfaces.IMapper;
import org.modelmapper.ModelMapper;

public class ProfileMapper implements IMapper<ProfileEntity, ProfileDTO> {
    private ModelMapper modelMapper;

    public ProfileMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @Override
    public ProfileDTO convertToDto(ProfileEntity profileEntity) {
        return modelMapper.map(profileEntity, ProfileDTO.class);
    }

    @Override
    public ProfileEntity convertToEntity(ProfileDTO profileDTO) {
        return modelMapper.map(profileDTO, ProfileEntity.class);
    }
}
