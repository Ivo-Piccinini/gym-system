package com.utnGymGroup.gym_system.features.routine;

import com.utnGymGroup.gym_system.common.interfaces.IMapper;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class RoutineMapper implements IMapper <RoutineEntity,RoutineDto>
{
   private ModelMapper modelMapper;

    public RoutineMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @Override
    public RoutineDto convertToDto(RoutineEntity routineEntity) {
        return modelMapper.map(routineEntity,RoutineDto.class);
    }

    @Override
    public RoutineEntity convertToEntity(RoutineDto routineDto) {
        return modelMapper.map(routineDto,RoutineEntity.class);
    }

    @Override
    public void updateEntityFromDTO(RoutineDto routineDto, RoutineEntity routineEntity) {
        modelMapper.map(routineDto, routineEntity);
    }
}
