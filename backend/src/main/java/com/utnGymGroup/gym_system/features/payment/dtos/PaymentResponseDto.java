package com.utnGymGroup.gym_system.features.payment.dtos;

import com.utnGymGroup.gym_system.features.payment.PaymentMethods;
import com.utnGymGroup.gym_system.features.payment.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private UUID subscriptionPublicId;
    private UUID publicId;
    private PaymentStatus status;
    private String validatedBy;
    private LocalDateTime validationDate;
    private String clientName;
    private String clientEmail;
    private String clientDni;
    private String planName;

}
