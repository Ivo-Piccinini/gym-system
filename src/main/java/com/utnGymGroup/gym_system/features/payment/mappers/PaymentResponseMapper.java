package com.utnGymGroup.gym_system.features.payment.mappers;

import com.utnGymGroup.gym_system.common.interfaces.IMapper;
import com.utnGymGroup.gym_system.features.payment.PaymentEntity;
import com.utnGymGroup.gym_system.features.payment.dtos.PaymentResponseDto;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentResponseMapper implements IMapper<PaymentEntity, PaymentResponseDto> {

    private final ModelMapper modelMapper;

    @Override
    public PaymentResponseDto convertToDto(PaymentEntity paymentEntity) {
        return modelMapper.map(paymentEntity, PaymentResponseDto.class);
    }

    @Override
    public PaymentEntity convertToEntity(PaymentResponseDto paymentResponseDto) {
        return modelMapper.map(paymentResponseDto, PaymentEntity.class);
    }

    @Override
    public void updateEntityFromDTO(PaymentResponseDto paymentResponseDto, PaymentEntity paymentEntity) {
        modelMapper.map(paymentResponseDto, paymentEntity);
    }
}