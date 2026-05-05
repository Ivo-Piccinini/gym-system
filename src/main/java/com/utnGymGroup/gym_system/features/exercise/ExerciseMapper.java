package com.utnGymGroup.gym_system.features.exercise;


import com.utnGymGroup.gym_system.common.interfaces.IMapper;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;


@Component
public class ExerciseMapper implements IMapper <ExerciseEntity,ExerciseDto> {

    private final ModelMapper modelMapper;

    public ExerciseMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @Override
    public ExerciseDto convertToDto(ExerciseEntity exerciseEntity) {
        return modelMapper.map(exerciseEntity,ExerciseDto.class);

    }

    @Override
    public ExerciseEntity convertToEntity(ExerciseDto exerciseDto) {
       return modelMapper.map(exerciseDto,ExerciseEntity.class);
    }
}
