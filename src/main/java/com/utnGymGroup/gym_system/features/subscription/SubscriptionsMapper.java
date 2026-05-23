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
    public SubscriptionsDTO convertToDto(SubscriptionsEntity subscriptionsEntity) {
        return modelMapper.map(subscriptionsEntity, SubscriptionsDTO.class);
    }

    @Override
    public void updateEntityFromDTO(SubscriptionsDTO subscriptionsDTO, SubscriptionsEntity subscriptionsEntity) {
        modelMapper.map(subscriptionsDTO, subscriptionsEntity);
    }

    @Override
    public SubscriptionsEntity convertToEntity(SubscriptionsDTO dto) {
        return modelMapper.map(dto, SubscriptionsEntity.class);
    }
}