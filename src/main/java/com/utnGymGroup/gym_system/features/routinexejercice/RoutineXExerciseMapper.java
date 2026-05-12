package com.utnGymGroup.gym_system.features.routinexejercice;

import com.utnGymGroup.gym_system.common.interfaces.IMapper;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class RoutineXExerciseMapper implements IMapper<RoutineXEjerciceEntity,RoutineXExerciseDto>
{
    private final ModelMapper modelMapper;


    public RoutineXExerciseMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @Override
    public RoutineXExerciseDto convertToDto(RoutineXEjerciceEntity routineXEjerciceEntity) {
        return modelMapper.map(routineXEjerciceEntity,RoutineXExerciseDto.class);
    }

    @Override
    public RoutineXEjerciceEntity convertToEntity(RoutineXExerciseDto routineXExerciseDto) {
        return modelMapper.map(routineXExerciseDto,RoutineXEjerciceEntity.class);
    }
}
