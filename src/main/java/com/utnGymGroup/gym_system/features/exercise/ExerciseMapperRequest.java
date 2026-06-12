package com.utnGymGroup.gym_system.features.exercise;


import com.utnGymGroup.gym_system.common.interfaces.IMapper;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;


@Component
public class ExerciseMapperRequest implements IMapper <ExerciseEntity, ExerciseDtoRequest> {

    private final ModelMapper modelMapper;

    public ExerciseMapperRequest(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @Override
    public ExerciseDtoRequest convertToDto(ExerciseEntity exerciseEntity) {
        return modelMapper.map(exerciseEntity, ExerciseDtoRequest.class);

    }

    @Override
    public ExerciseEntity convertToEntity(ExerciseDtoRequest exerciseDtoRequest) {
       return modelMapper.map(exerciseDtoRequest,ExerciseEntity.class);
    }

    @Override
    public void updateEntityFromDTO(ExerciseDtoRequest exerciseDtoRequest, ExerciseEntity exerciseEntity) {
        modelMapper.map(exerciseDtoRequest, exerciseEntity);
    }
}
