package com.utnGymGroup.gym_system.common.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Solicitud de inicio de sesión del usuario")
public record AuthRequest(
        @Schema(description = "Nombre de usuario único o DNI", example = "admin", requiredMode = Schema.RequiredMode.REQUIRED)
        String username,
        
        @Schema(description = "Contraseña de acceso", example = "password123", requiredMode = Schema.RequiredMode.REQUIRED)
        String password
) {
}

