package com.utnGymGroup.gym_system.features.payments;

import com.utnGymGroup.gym_system.common.interfaces.IMapper;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class PaymentsMapper implements IMapper<PaymentsEntity, PaymentsDTO> {

    private final ModelMapper modelMapper;

    public PaymentsMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @Override
    public PaymentsDTO convertToDto(PaymentsEntity entity) {

        PaymentsDTO dto = modelMapper.map(entity, PaymentsDTO.class);


        if (entity.getSubscription() != null) {
            dto.setSubscriptionId(entity.getSubscription().getId());
        }

        return dto;
    }

    @Override
    public PaymentsEntity convertToEntity(PaymentsDTO dto) {

        return modelMapper.map(dto, PaymentsEntity.class);
    }
}