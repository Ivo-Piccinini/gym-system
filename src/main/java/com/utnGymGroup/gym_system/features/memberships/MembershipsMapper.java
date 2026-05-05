package com.utnGymGroup.gym_system.features.memberships;

import com.utnGymGroup.gym_system.common.interfaces.IMapper;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class MembershipsMapper implements IMapper<MembershipsEntity, MembershipsDTO> {

    private final ModelMapper modelMapper;

    public MembershipsMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @Override
    public MembershipsDTO convertToDto(MembershipsEntity entity) {
        return modelMapper.map(entity, MembershipsDTO.class);
    }

    @Override
    public MembershipsEntity convertToEntity(MembershipsDTO dto) {
        return modelMapper.map(dto, MembershipsEntity.class);
    }
}