package com.utnGymGroup.gym_system.features.activity;

import com.utnGymGroup.gym_system.common.interfaces.IMapper;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component // IMPORTANTE: Para que Spring lo pueda inyectar en los Services
public class ActivityMapper implements IMapper<ActivityEntity, ActivityDTO> {

    private final ModelMapper modelMapper;

    // Spring inyecta automáticamente el Bean que definieron en MapperConfig
    public ActivityMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @Override
    public ActivityDTO convertToDto(ActivityEntity entity) {
        return modelMapper.map(entity, ActivityDTO.class);
    }

    @Override
    public ActivityEntity convertToEntity(ActivityDTO dto) {
        return modelMapper.map(dto, ActivityEntity.class);
    }

    @Override
    public void updateEntityFromDTO(ActivityDTO activityDTO, ActivityEntity activityEntity) {
        modelMapper.map(activityDTO,activityEntity);
    }
}