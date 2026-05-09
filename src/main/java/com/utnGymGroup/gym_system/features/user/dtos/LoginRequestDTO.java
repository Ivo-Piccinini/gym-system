package com.utnGymGroup.gym_system.features.user.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

// DTO para recibir las credenciales del usuario

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class LoginRequestDTO {
    @NotBlank(message = "El nombre de usuario es requerido.")
    private String username;

    @NotBlank(message = "La contraseña es requerida.")
    private String password;
}
