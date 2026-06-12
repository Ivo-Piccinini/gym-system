package com.utnGymGroup.gym_system.features.fullRoutine;

import com.utnGymGroup.gym_system.common.interfaces.IMapper;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class FullRoutineMapper implements IMapper<FullRoutineEntity, FullRoutineDto>
{
    private final ModelMapper modelMapper;


    public FullRoutineMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @Override
    public FullRoutineDto convertToDto(FullRoutineEntity fullRoutineEntity) {
        return modelMapper.map(fullRoutineEntity, FullRoutineDto.class);
    }

    @Override
    public FullRoutineEntity convertToEntity(FullRoutineDto fullRoutineDto) {
        return modelMapper.map(fullRoutineDto, FullRoutineEntity.class);
    }

    @Override
    public void updateEntityFromDTO(FullRoutineDto fullRoutineDto, FullRoutineEntity routineXEjerciceEntity) {
        modelMapper.map(fullRoutineDto,routineXEjerciceEntity);
    }
}
