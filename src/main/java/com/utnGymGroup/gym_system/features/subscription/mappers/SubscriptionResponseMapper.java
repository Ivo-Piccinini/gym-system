package com.utnGymGroup.gym_system.features.subscription.mappers;

import com.utnGymGroup.gym_system.common.interfaces.IMapper;
import com.utnGymGroup.gym_system.features.subscription.SubscriptionEntity;
import com.utnGymGroup.gym_system.features.subscription.dtos.SubscriptionResponseDto;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SubscriptionResponseMapper implements IMapper<SubscriptionEntity, SubscriptionResponseDto> {

    private final ModelMapper modelMapper;

    @Override
    public SubscriptionResponseDto convertToDto(SubscriptionEntity subscriptionEntity) {
        return modelMapper.map(subscriptionEntity, SubscriptionResponseDto.class);
    }

    @Override
    public SubscriptionEntity convertToEntity(SubscriptionResponseDto subscriptionResponseDto) {
        return modelMapper.map(subscriptionResponseDto, SubscriptionEntity.class);
    }

    @Override
    public void updateEntityFromDTO(SubscriptionResponseDto subscriptionResponseDto, SubscriptionEntity subscriptionEntity) {
        modelMapper.map(subscriptionResponseDto, subscriptionEntity);
    }
}