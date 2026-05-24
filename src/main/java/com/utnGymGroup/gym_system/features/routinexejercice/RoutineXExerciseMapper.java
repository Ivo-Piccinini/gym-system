package com.utnGymGroup.gym_system.features.routinexejercice;

import com.utnGymGroup.gym_system.common.interfaces.IMapper;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class RoutineXExerciseMapper implements IMapper<RoutineXExerciceEntity,RoutineXExerciseDto>
{
    private final ModelMapper modelMapper;


    public RoutineXExerciseMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @Override
    public RoutineXExerciseDto convertToDto(RoutineXExerciceEntity routineXExerciceEntity) {
        return modelMapper.map(routineXExerciceEntity,RoutineXExerciseDto.class);
    }

    @Override
    public RoutineXExerciceEntity convertToEntity(RoutineXExerciseDto routineXExerciseDto) {
        return modelMapper.map(routineXExerciseDto, RoutineXExerciceEntity.class);
    }

    @Override
    public void updateEntityFromDTO(RoutineXExerciseDto routineXExerciseDto, RoutineXEjerciceEntity routineXEjerciceEntity) {
        modelMapper.map(routineXExerciseDto,routineXEjerciceEntity);
    }
}
