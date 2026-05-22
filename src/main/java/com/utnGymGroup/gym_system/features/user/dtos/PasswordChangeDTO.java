package com.utnGymGroup.gym_system.features.user.dtos;

import com.utnGymGroup.gym_system.common.interfaces.IUpdate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PasswordChangeDTO {
    @NotBlank(groups = IUpdate.class, message = "La contraseña anterior es requerida.")
    private String oldPassword;

    @NotBlank(groups = IUpdate.class, message = "La nueva contraseña es requerida.")
    @Size(min = 8, groups = IUpdate.class, message = "La nueva contraseña debe tener minimo 8 caracteres.")
    private String newPassword;

    @NotBlank(groups = IUpdate.class, message = "La confirmación de la nueva contraseña es requerida.")
    private String confirmPassword;
}
