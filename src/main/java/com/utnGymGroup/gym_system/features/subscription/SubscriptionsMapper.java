package com.utnGymGroup.gym_system.features.subscription;

import com.utnGymGroup.gym_system.common.interfaces.IMapper;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionsMapper implements IMapper<SubscriptionsEntity, SubscriptionsDTO> {

    private final ModelMapper modelMapper;


    public SubscriptionsMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @Override
    public SubscriptionsDTO convertToDto(SubscriptionsEntity entity) {
        SubscriptionsDTO dto = modelMapper.map(entity, SubscriptionsDTO.class);


        if (entity.getUser() != null) {
            dto.setUserId(entity.getUser().getId());
        }


        if (entity.getPlanId() != null) {
            dto.setPlanId(entity.getPlan().getId());
        }

        return dto;
    }

    @Override
    public SubscriptionsEntity convertToEntity(SubscriptionsDTO dto) {
        return modelMapper.map(dto, SubscriptionsEntity.class);
    }
}