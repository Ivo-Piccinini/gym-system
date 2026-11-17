package com.utnGymGroup.gym_system.features.activity;

import java.util.UUID;

import com.utnGymGroup.gym_system.common.interfaces.ICreate;
import com.utnGymGroup.gym_system.common.interfaces.IUpdate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter

public class ActivityDTO {
    @NotNull(message = "El ID externo es requerido para actualizar", groups = IUpdate.class)
    private UUID externalId;

    @NotBlank(message = "el nombre de la actividad es obligatorio", groups = ICreate.class)
    @Size(min = 3, max = 50, message = "El nombre debe tener entre 3 y 50 caracteres", groups = {ICreate.class, IUpdate.class})
    private String name;

    @NotBlank(message = "La descripción es obligatoria", groups = ICreate.class)
    @Size(max = 255, message = "La descripción no puede superar los 255 caracteres", groups = {ICreate.class, IUpdate.class})
    private String description;
}
