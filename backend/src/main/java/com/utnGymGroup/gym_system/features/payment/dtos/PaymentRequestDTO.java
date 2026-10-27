package com.utnGymGroup.gym_system.features.payment.dtos;

import com.utnGymGroup.gym_system.common.interfaces.ICreate;
import com.utnGymGroup.gym_system.common.interfaces.IUpdate;
import com.utnGymGroup.gym_system.features.payment.PaymentMethods;
import jakarta.validation.constraints.*;
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
public class PaymentRequestDTO {

    @NotNull(message = "El monto es obligatorio", groups = ICreate.class)
    @Positive(message = "El monto debe ser un valor positivo", groups = {ICreate.class,IUpdate.class})
    private Double amount;

    @NotNull(message = "La fecha es obligatoria", groups = ICreate.class)
    private LocalDate paymentDate;

    @NotNull(message = "El método de pago es obligatorio", groups = ICreate.class)
    private PaymentMethods method;

    @NotNull(message = "El ID de la suscripción es obligatorio", groups = ICreate.class)
    private UUID subscriptionId;
}