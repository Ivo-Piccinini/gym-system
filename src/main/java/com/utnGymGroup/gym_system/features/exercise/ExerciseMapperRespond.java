package com.utnGymGroup.gym_system.features.exercise;

import com.utnGymGroup.gym_system.common.interfaces.IMapper;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;


@Component
public class ExerciseMapperRespond implements IMapper<ExerciseEntity,ExerciseDtoRespond>
{
    private final ModelMapper modelMapper;

    public ExerciseMapperRespond(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @Override
    public ExerciseDtoRespond convertToDto(ExerciseEntity exerciseEntity) {
        return modelMapper.map(exerciseEntity, ExerciseDtoRespond.class) ;
    }

    @Override
    public ExerciseEntity convertToEntity(ExerciseDtoRespond exerciseDtoRespond) {
        return modelMapper.map(exerciseDtoRespond, ExerciseEntity.class);
    }

    @Override
    public void updateEntityFromDTO(ExerciseDtoRespond exerciseDtoRespond, ExerciseEntity exerciseEntity) {

        modelMapper.map(exerciseDtoRespond , exerciseEntity);
    }
}
