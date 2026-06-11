package com.utnGymGroup.gym_system.features.payment.mappers;

import com.utnGymGroup.gym_system.common.interfaces.IMapper;
import com.utnGymGroup.gym_system.features.payment.PaymentEntity;
import com.utnGymGroup.gym_system.features.payment.dtos.PaymentRequestDTO;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentRequestMapper implements IMapper<PaymentEntity, PaymentRequestDTO> {

    private final ModelMapper modelMapper;

    @Override
    public PaymentRequestDTO convertToDto(PaymentEntity paymentEntity) {

        return modelMapper.map(paymentEntity, PaymentRequestDTO.class);
    }

    @Override
    public PaymentEntity convertToEntity(PaymentRequestDTO paymentRequestDTO) {

        return modelMapper.map(paymentRequestDTO, PaymentEntity.class);
    }

    @Override
    public void updateEntityFromDTO(PaymentRequestDTO paymentRequestDTO, PaymentEntity paymentEntity) {

        modelMapper.map(paymentRequestDTO, paymentEntity);
    }
}