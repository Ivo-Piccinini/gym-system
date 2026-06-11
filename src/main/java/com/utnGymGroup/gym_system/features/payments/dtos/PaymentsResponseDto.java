package com.utnGymGroup.gym_system.features.payments.dtos;

import com.utnGymGroup.gym_system.features.payments.PaymentMethods;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PaymentsResponseDto {

    private Long id;
    private Double amount;
    private LocalDate paymentDate;
    private PaymentMethods method;
    private Long subscriptionId;

}
