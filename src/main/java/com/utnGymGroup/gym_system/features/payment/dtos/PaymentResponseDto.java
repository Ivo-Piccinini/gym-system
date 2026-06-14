package com.utnGymGroup.gym_system.features.payment.dtos;

import com.utnGymGroup.gym_system.features.payment.PaymentMethods;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PaymentResponseDto {

    private Long id;
    private Double amount;
    private LocalDate paymentDate;
    private PaymentMethods method;
    private Long subscriptionId;
    private UUID publicId;

}
