package com.utnGymGroup.gym_system.features.exercise;

import com.utnGymGroup.gym_system.common.interfaces.IMapper;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;


@Component
public class ExerciseMapperRespond implements IMapper<ExerciseEntity, ExerciseDtoResponse>
{
    private final ModelMapper modelMapper;

    public ExerciseMapperRespond(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @Override
    public ExerciseDtoResponse convertToDto(ExerciseEntity exerciseEntity) {
        return modelMapper.map(exerciseEntity, ExerciseDtoResponse.class) ;
    }

    @Override
    public ExerciseEntity convertToEntity(ExerciseDtoResponse exerciseDtoResponse) {
        return modelMapper.map(exerciseDtoResponse, ExerciseEntity.class);
    }

    @Override
    public void updateEntityFromDTO(ExerciseDtoResponse exerciseDtoResponse, ExerciseEntity exerciseEntity) {

        modelMapper.map(exerciseDtoResponse, exerciseEntity);
    }
}
