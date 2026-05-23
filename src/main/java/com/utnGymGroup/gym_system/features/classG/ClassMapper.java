package com.utnGymGroup.gym_system.features.classG;

import com.utnGymGroup.gym_system.common.interfaces.IMapper;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class ClassMapper implements IMapper<ClassEntity, ClassDTO> {

    private final ModelMapper modelMapper;

    public ClassMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @Override
    public ClassDTO convertToDto(ClassEntity entity) {
        return modelMapper.map(entity, ClassDTO.class);
    }

    @Override
    public ClassEntity convertToEntity(ClassDTO dto) {
        return modelMapper.map(dto, ClassEntity.class);
    }

    @Override
    public void updateEntityFromDTO(ClassDTO classDTO, ClassEntity classEntity) {
        modelMapper.map(classDTO, classEntity);
    }
}