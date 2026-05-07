package com.utnGymGroup.gym_system.features.enrollment;

import com.utnGymGroup.gym_system.common.interfaces.IMapper;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class EnrollmentMapper implements IMapper<EnrollmentEntity, EnrollmentDTO> {

    private final ModelMapper modelMapper;

    public EnrollmentMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @Override
    public EnrollmentDTO convertToDto(EnrollmentEntity entity) {
        if (entity == null) return null;
        return modelMapper.map(entity, EnrollmentDTO.class);
    }

    @Override
    public EnrollmentEntity convertToEntity(EnrollmentDTO dto) {
        if (dto == null) return null;
        return modelMapper.map(dto, EnrollmentEntity.class);
    }
}
