package com.utnGymGroup.gym_system.features.payments.mappers;

import com.utnGymGroup.gym_system.common.interfaces.IMapper;
import com.utnGymGroup.gym_system.features.payments.PaymentsEntity;
import com.utnGymGroup.gym_system.features.payments.dtos.PaymentsResponseDto;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentsResponseMapper implements IMapper<PaymentsEntity, PaymentsResponseDto> {

    private final ModelMapper modelMapper;

    @Override
    public PaymentsResponseDto convertToDto(PaymentsEntity paymentsEntity) {
        return modelMapper.map(paymentsEntity, PaymentsResponseDto.class);
    }

    @Override
    public PaymentsEntity convertToEntity(PaymentsResponseDto paymentsResponseDto) {
        return modelMapper.map(paymentsResponseDto, PaymentsEntity.class);
    }

    @Override
    public void updateEntityFromDTO(PaymentsResponseDto paymentsResponseDto, PaymentsEntity paymentsEntity) {
        modelMapper.map(paymentsResponseDto, paymentsEntity);
    }
}