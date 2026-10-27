package com.utnGymGroup.gym_system.features.user.dtos;

import com.utnGymGroup.gym_system.common.interfaces.IUpdate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Schema(description = "DTO para la solicitud de cambio de contraseña")
public class PasswordChangeDTO {
    @Schema(description = "Contraseña actual del usuario", example = "password123", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(groups = IUpdate.class, message = "La contraseña anterior es requerida.")
    private String oldPassword;

    @Schema(description = "Nueva contraseña deseada (mínimo 8 caracteres)", example = "nuevaclave123", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(groups = IUpdate.class, message = "La nueva contraseña es requerida.")
    @Size(min = 8, groups = IUpdate.class, message = "La nueva contraseña debe tener minimo 8 caracteres.")
    private String newPassword;

    @Schema(description = "Confirmación exacta de la nueva contraseña", example = "nuevaclave123", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(groups = IUpdate.class, message = "La confirmación de la nueva contraseña es requerida.")
    private String confirmPassword;
}

