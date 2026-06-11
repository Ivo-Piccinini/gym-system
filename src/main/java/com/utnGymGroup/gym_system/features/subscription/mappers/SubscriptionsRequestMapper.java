package com.utnGymGroup.gym_system.features.subscription.mappers;

import com.utnGymGroup.gym_system.common.interfaces.IMapper;
import com.utnGymGroup.gym_system.features.subscription.SubscriptionsEntity;
import com.utnGymGroup.gym_system.features.subscription.dtos.SubscriptionsRequestDTO;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SubscriptionsRequestMapper implements IMapper<SubscriptionsEntity, SubscriptionsRequestDTO> {

    private final ModelMapper modelMapper;

    @Override
    public SubscriptionsRequestDTO convertToDto(SubscriptionsEntity subscriptionsEntity) {
        return modelMapper.map(subscriptionsEntity, SubscriptionsRequestDTO.class);
    }

    @Override
    public SubscriptionsEntity convertToEntity(SubscriptionsRequestDTO subscriptionsRequestDTO) {
        return modelMapper.map(subscriptionsRequestDTO, SubscriptionsEntity.class);
    }

    @Override
    public void updateEntityFromDTO(SubscriptionsRequestDTO subscriptionsRequestDTO, SubscriptionsEntity subscriptionsEntity) {
        modelMapper.map(subscriptionsRequestDTO, subscriptionsEntity);
    }
}