package com.utnGymGroup.gym_system.features.subscription.mappers;

import com.utnGymGroup.gym_system.common.interfaces.IMapper;
import com.utnGymGroup.gym_system.features.subscription.SubscriptionEntity;
import com.utnGymGroup.gym_system.features.subscription.dtos.SubscriptionRequestDTO;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SubscriptionRequestMapper implements IMapper<SubscriptionEntity, SubscriptionRequestDTO> {

    private final ModelMapper modelMapper;

    @Override
    public SubscriptionRequestDTO convertToDto(SubscriptionEntity subscriptionEntity) {
        return modelMapper.map(subscriptionEntity, SubscriptionRequestDTO.class);
    }

    @Override
    public SubscriptionEntity convertToEntity(SubscriptionRequestDTO subscriptionRequestDTO) {
        return modelMapper.map(subscriptionRequestDTO, SubscriptionEntity.class);
    }

    @Override
    public void updateEntityFromDTO(SubscriptionRequestDTO subscriptionRequestDTO, SubscriptionEntity subscriptionEntity) {
        modelMapper.map(subscriptionRequestDTO, subscriptionEntity);
    }
}