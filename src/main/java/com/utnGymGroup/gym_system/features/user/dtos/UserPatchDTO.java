package com.utnGymGroup.gym_system.features.user.dtos;

import com.utnGymGroup.gym_system.common.interfaces.ICreate;
import com.utnGymGroup.gym_system.common.interfaces.IUpdate;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class UserPatchDTO {
    @Size(min = 3, max = 20, message = "El nombre del usuario debe tener entre 3 y 20 caracteres.", groups = {ICreate.class, IUpdate.class})
    private String firstName;

    @Size(min = 3, max = 30, message = "El apellido del usuario debe tener entre 3 y 30 caracteres.", groups = {ICreate.class, IUpdate.class})
    private String lastName;

    private String phone;

    private LocalDate birthDay;
}
