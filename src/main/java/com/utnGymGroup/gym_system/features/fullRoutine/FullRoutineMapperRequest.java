package com.utnGymGroup.gym_system.features.fullRoutine;

import com.utnGymGroup.gym_system.common.interfaces.IMapper;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class FullRoutineMapperRequest implements IMapper<FullRoutineEntity, FullRoutineDtoRequest>
{
    private final ModelMapper modelMapper;

    public FullRoutineMapperRequest(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @Override
    public FullRoutineDtoRequest convertToDto(FullRoutineEntity fullRoutineEntity) {
        return modelMapper.map(fullRoutineEntity, FullRoutineDtoRequest.class);
    }

    @Override
    public FullRoutineEntity convertToEntity(FullRoutineDtoRequest fullRoutineDto) {
        return modelMapper.map(fullRoutineDto, FullRoutineEntity.class);
    }

    @Override
    public void updateEntityFromDTO(FullRoutineDtoRequest fullRoutineDto, FullRoutineEntity routineXEjerciceEntity) {
        modelMapper.map(fullRoutineDto,routineXEjerciceEntity);
    }
}
