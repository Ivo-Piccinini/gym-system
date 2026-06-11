package com.utnGymGroup.gym_system.features.payments.mappers;

import com.utnGymGroup.gym_system.common.interfaces.IMapper;
import com.utnGymGroup.gym_system.features.payments.PaymentsEntity;
import com.utnGymGroup.gym_system.features.payments.dtos.PaymentsRequestDTO;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentRequestMapper implements IMapper<PaymentsEntity, PaymentsRequestDTO> {

    private final ModelMapper modelMapper;

    @Override
    public PaymentsRequestDTO convertToDto(PaymentsEntity paymentsEntity) {

        return modelMapper.map(paymentsEntity, PaymentsRequestDTO.class);
    }

    @Override
    public PaymentsEntity convertToEntity(PaymentsRequestDTO paymentsRequestDTO) {

        return modelMapper.map(paymentsRequestDTO, PaymentsEntity.class);
    }

    @Override
    public void updateEntityFromDTO(PaymentsRequestDTO paymentsRequestDTO, PaymentsEntity paymentsEntity) {

        modelMapper.map(paymentsRequestDTO, paymentsEntity);
    }
}