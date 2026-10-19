package com.utnGymGroup.gym_system.features.routine;

import com.utnGymGroup.gym_system.common.interfaces.IMapper;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class RoutineMapperRequest implements IMapper <RoutineEntity, RoutineRequestDto>
{
   private ModelMapper modelMapper;

    public RoutineMapperRequest(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @Override
    public RoutineRequestDto convertToDto(RoutineEntity routineEntity) {
        return modelMapper.map(routineEntity, RoutineRequestDto.class);
    }

    @Override
    public RoutineEntity convertToEntity(RoutineRequestDto routineDto) {
        return modelMapper.map(routineDto,RoutineEntity.class);
    }

    @Override
    public void updateEntityFromDTO(RoutineRequestDto routineDto, RoutineEntity routineEntity) {
        modelMapper.map(routineDto, routineEntity);
    }
}
