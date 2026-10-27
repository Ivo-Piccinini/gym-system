package com.utnGymGroup.gym_system.features.user.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Solicitud para personalizar el nombre de usuario por primera vez")
public record UsernameChangeDTO (
        @Schema(description = "Nuevo nombre de usuario único en el sistema", example = "cliente1nuevo", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "El nuevo username es requerido.")
        @Size(min = 3, max = 20, message = "El username debe tener entre 3 y 20 caracteres.")
        String newUsername
){}

