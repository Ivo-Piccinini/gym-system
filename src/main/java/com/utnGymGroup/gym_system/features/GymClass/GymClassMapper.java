package com.utnGymGroup.gym_system.features.GymClass;

import com.utnGymGroup.gym_system.common.interfaces.IMapper;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class GymClassMapper implements IMapper<GymClassEntity, GymClassDTO> {

    private final ModelMapper modelMapper;

    public GymClassMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @Override
    public GymClassDTO convertToDto(GymClassEntity entity) {
        return modelMapper.map(entity, GymClassDTO.class);
    }

    @Override
    public GymClassEntity convertToEntity(GymClassDTO dto) {
        return modelMapper.map(dto, GymClassEntity.class);
    }

    @Override
    public void updateEntityFromDTO(GymClassDTO gymClassDTO, GymClassEntity gymClassEntity) {
        modelMapper.map(gymClassDTO, gymClassEntity);
    }
}