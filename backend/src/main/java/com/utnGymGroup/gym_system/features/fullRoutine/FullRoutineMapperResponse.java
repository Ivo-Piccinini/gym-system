package com.utnGymGroup.gym_system.features.fullRoutine;

import com.utnGymGroup.gym_system.common.interfaces.IMapper;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FullRoutineMapperResponse implements IMapper<FullRoutineEntity, FullRoutineDtoResponse>
{
    private final ModelMapper modelMapper;

    public FullRoutineMapperResponse(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @Override
    public FullRoutineDtoResponse convertToDto(FullRoutineEntity fullRoutineEntity) {
        return modelMapper.map(fullRoutineEntity, FullRoutineDtoResponse.class);
    }

    @Override
    public FullRoutineEntity convertToEntity(FullRoutineDtoResponse fullRoutineDtoResponse) {
        return modelMapper.map(fullRoutineDtoResponse, FullRoutineEntity.class);
    }

    @Override
    public void updateEntityFromDTO(FullRoutineDtoResponse fullRoutineDtoResponse, FullRoutineEntity fullRoutineEntity) {

        modelMapper.map(fullRoutineDtoResponse,fullRoutineDtoResponse);
    }
}
