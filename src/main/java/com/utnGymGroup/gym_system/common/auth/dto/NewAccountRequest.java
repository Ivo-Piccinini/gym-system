package com.utnGymGroup.gym_system.common.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Solicitud para el registro autónomo de un nuevo cliente")
public record NewAccountRequest(
        @Schema(description = "Nombre de usuario único", example = "nuevocliente", requiredMode = Schema.RequiredMode.REQUIRED)
        String username,
        
        @Schema(description = "Contraseña de acceso (mínimo 8 caracteres)", example = "password123", requiredMode = Schema.RequiredMode.REQUIRED)
        String password,
        
        @Schema(description = "Correo electrónico del cliente", example = "nuevocliente@gym.com", requiredMode = Schema.RequiredMode.REQUIRED)
        String email,
        
        @Schema(description = "DNI o identificación nacional del cliente", example = "99998888", requiredMode = Schema.RequiredMode.REQUIRED)
        String dni
) {
}

