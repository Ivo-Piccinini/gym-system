package com.utnGymGroup.gym_system.features.payment.mappers;

import com.utnGymGroup.gym_system.common.interfaces.IMapper;
import com.utnGymGroup.gym_system.features.payment.PaymentEntity;
import com.utnGymGroup.gym_system.features.payment.dtos.PaymentResponseDto;
import com.utnGymGroup.gym_system.features.subscription.SubscriptionEntity;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentResponseMapper implements IMapper<PaymentEntity, PaymentResponseDto> {

    private final ModelMapper modelMapper;

    @Override
    public PaymentResponseDto convertToDto(PaymentEntity paymentEntity) {
        PaymentResponseDto dto = modelMapper.map(paymentEntity, PaymentResponseDto.class);
        dto.setPublicId(paymentEntity.getPublicId());
        dto.setStatus(paymentEntity.getStatus());
        dto.setValidatedBy(paymentEntity.getValidatedBy());
        dto.setValidationDate(paymentEntity.getValidationDate());

        SubscriptionEntity sub = paymentEntity.getSubscription();
        if (sub != null) {
            dto.setSubscriptionId(sub.getId());
            dto.setSubscriptionPublicId(sub.getPublicId());
            if (sub.getPlan() != null) {
                dto.setPlanName(sub.getPlan().getName());
            }
            if (sub.getUser() != null) {
                String first = sub.getUser().getFirstName() != null ? sub.getUser().getFirstName() : "";
                String last = sub.getUser().getLastName() != null ? sub.getUser().getLastName() : "";
                String fullName = (first + " " + last).trim();
                dto.setClientName(fullName.isEmpty() ? sub.getUser().getEmail() : fullName);
                dto.setClientEmail(sub.getUser().getEmail());
                dto.setClientDni(sub.getUser().getDni());
            }
        }
        return dto;
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
