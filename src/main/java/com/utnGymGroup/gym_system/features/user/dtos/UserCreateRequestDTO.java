package com.utnGymGroup.gym_system.features.user.dtos;

import com.utnGymGroup.gym_system.common.interfaces.ICreate;
import com.utnGymGroup.gym_system.common.interfaces.IUpdate;
import com.utnGymGroup.gym_system.features.profile.ProfileDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserCreateRequestDTO {
    @NotBlank(message = "El username es obligatorio", groups = IUpdate.class)
    @Size(min = 4, message = "El username debe tener más de 4 caracteres.", groups = ICreate.class)
    private String username;

    @NotBlank(message = "La contraseña es obligatoria", groups = ICreate.class)
    @Size(min = 8, message = "La contraseña debe tener mínimo 8 caracteres.", groups = ICreate.class)
    private String password;

    @Email(groups = ICreate.class)
    private String email;

    @NotNull(groups = ICreate.class)
    @Valid
    private ProfileDTO profile;
}
