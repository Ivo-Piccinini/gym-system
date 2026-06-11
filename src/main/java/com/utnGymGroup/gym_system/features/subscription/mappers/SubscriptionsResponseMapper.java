package com.utnGymGroup.gym_system.features.subscription.mappers;

import com.utnGymGroup.gym_system.common.interfaces.IMapper;
import com.utnGymGroup.gym_system.features.subscription.SubscriptionsEntity;
import com.utnGymGroup.gym_system.features.subscription.dtos.SubscriptionsResponseDto;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SubscriptionsResponseMapper implements IMapper<SubscriptionsEntity, SubscriptionsResponseDto> {

    private final ModelMapper modelMapper;

    @Override
    public SubscriptionsResponseDto convertToDto(SubscriptionsEntity subscriptionsEntity) {
        return modelMapper.map(subscriptionsEntity, SubscriptionsResponseDto.class);
    }

    @Override
    public SubscriptionsEntity convertToEntity(SubscriptionsResponseDto subscriptionsResponseDto) {
        return modelMapper.map(subscriptionsResponseDto, SubscriptionsEntity.class);
    }

    @Override
    public void updateEntityFromDTO(SubscriptionsResponseDto subscriptionsResponseDto, SubscriptionsEntity subscriptionsEntity) {
        modelMapper.map(subscriptionsResponseDto, subscriptionsEntity);
    }
}