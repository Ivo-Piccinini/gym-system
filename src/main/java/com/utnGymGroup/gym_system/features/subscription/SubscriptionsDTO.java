package com.utnGymGroup.gym_system.features.subscription;

import com.utnGymGroup.gym_system.common.interfaces.ICreate;
import com.utnGymGroup.gym_system.common.interfaces.IUpdate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SubscriptionsDTO {

    @NotNull(message = "El ID de usuario es obligatorio", groups = ICreate.class)
    private Long userId;

    @NotNull(message = "El ID del plan es obligatorio", groups = ICreate.class)
    private Long planId;

    @NotNull(message = "La fecha de inicio es requerida", groups = ICreate.class)
    private LocalDate startDate;

    private LocalDate endDate;

    @NotBlank(message = "El estado es requerido", groups = {ICreate.class, IUpdate.class})
    private String status;
}
