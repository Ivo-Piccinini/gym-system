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
    @NotBlank(groups = IUpdate.class)
    private String oldPassword;

    @NotBlank(groups = IUpdate.class)
    @Size(min = 8, groups = IUpdate.class)
    private String newPassword;

    @NotBlank(groups = IUpdate.class)
    private String confirmPassword;
}
