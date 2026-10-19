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
        ExerciseDtoRequest dto = modelMapper.map(exerciseEntity, ExerciseDtoRequest.class);
        if (exerciseEntity.getMuscleGroup() != null) {
            dto.setMuscle_group(exerciseEntity.getMuscleGroup().name());
        }
        return dto;
    }

    @Override
    public ExerciseEntity convertToEntity(ExerciseDtoRequest exerciseDtoRequest) {
        ExerciseEntity entity = modelMapper.map(exerciseDtoRequest, ExerciseEntity.class);
        if (exerciseDtoRequest.getMuscle_group() != null && !exerciseDtoRequest.getMuscle_group().isBlank()) {
            try {
                entity.setMuscleGroup(MuscleGroup.valueOf(exerciseDtoRequest.getMuscle_group().toUpperCase()));
            } catch (IllegalArgumentException e) {
                entity.setMuscleGroup(null);
            }
        }
        return entity;
    }

    @Override
    public void updateEntityFromDTO(ExerciseDtoRequest exerciseDtoRequest, ExerciseEntity exerciseEntity) {
        modelMapper.map(exerciseDtoRequest, exerciseEntity);
        if (exerciseDtoRequest.getMuscle_group() != null && !exerciseDtoRequest.getMuscle_group().isBlank()) {
            try {
                exerciseEntity.setMuscleGroup(MuscleGroup.valueOf(exerciseDtoRequest.getMuscle_group().toUpperCase()));
            } catch (IllegalArgumentException e) {
                exerciseEntity.setMuscleGroup(null);
            }
        }
    }
}
