package com.utnGymGroup.gym_system.features.routine;

import com.utnGymGroup.gym_system.common.interfaces.IMapper;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RoutineMapperResponse implements IMapper<RoutineEntity,RoutineResponseDto>
{
    private ModelMapper modelMapper;

    public RoutineMapperResponse(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @Override
    public RoutineResponseDto convertToDto(RoutineEntity routineEntity) {
        return modelMapper.map(routineEntity, RoutineResponseDto.class);
    }

    @Override
    public RoutineEntity convertToEntity(RoutineResponseDto routineDto) {
        return modelMapper.map(routineDto,RoutineEntity.class);
    }

    @Override
    public void updateEntityFromDTO(RoutineResponseDto routineDto, RoutineEntity routineEntity) {
        modelMapper.map(routineDto, routineEntity);
    }
}
