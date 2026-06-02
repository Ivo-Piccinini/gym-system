package com.utnGymGroup.gym_system.features.user.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UsernameChangeDTO (
        @NotBlank(message = "El nuevo username es requerido.")
        @Size(min = 3, max = 20, message = "El username debe tener entre 3 y 20 caracteres.")
        String newUsername
){}
