package com.utnGymGroup.gym_system.features.membership.dtos;

import com.utnGymGroup.gym_system.common.interfaces.ICreate;
import com.utnGymGroup.gym_system.common.interfaces.IUpdate;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MembershipRequestDTO {

    private Long id;

    @NotBlank(message = "El nombre del plan es obligatorio", groups = {ICreate.class, IUpdate.class})
    @Size(max = 50, message = "El nombre no puede superar los 50 caracteres")
    private String name;

    @NotNull(message = "El precio es obligatorio", groups = ICreate.class)
    @Positive(message = "El precio debe ser un valor positivo", groups = {ICreate.class, IUpdate.class})
    private Double price;

    @NotNull(message = "La duración en días es obligatoria", groups = ICreate.class)
    @Min(value = 1, message = "La duración mínima debe ser de 1 día", groups = {ICreate.class, IUpdate.class})
    private Integer durationDays;
}