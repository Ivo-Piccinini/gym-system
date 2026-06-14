package com.utnGymGroup.gym_system.common.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta de autenticación exitosa")
public record AuthResponse(
        @Schema(description = "Token JWT de acceso para las peticiones autorizadas", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String token,
        
        @Schema(description = "Mensaje aclaratorio del estado de autenticación", example = "Autenticación exitosa")
        String message
) {
}

