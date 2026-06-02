package com.utnGymGroup.gym_system.features.user.dtos;

import com.utnGymGroup.gym_system.common.auth.permissions.Roles;
import com.utnGymGroup.gym_system.common.interfaces.ICreate;
import com.utnGymGroup.gym_system.common.interfaces.IUpdate;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserDTO {

    @NotNull(message = "El id público es requerido.", groups = IUpdate.class)
    private UUID publicId;

    @Size(min = 3, max = 20, message = "El nombre del usuario debe tener entre 3 y 20 caracteres.", groups = {ICreate.class, IUpdate.class})
    private String firstName;

    @Size(min = 3, max = 30, message = "El apellido del usuario debe tener entre 3 y 30 caracteres.", groups = {ICreate.class, IUpdate.class})
    private String lastName;

    @Email(groups = {ICreate.class, IUpdate.class})
    @NotNull(message = "El email del usuario es requerido.", groups = ICreate.class)
    private String email;

    @NotNull(message = "El dni del usuario es requerido.", groups = ICreate.class)
    @Size(min = 8, max = 8, message = "El DNI del usuario debe contener 8 caracteres.", groups = {ICreate.class, IUpdate.class})
    private String dni;

    private String phone;

    private LocalDate birthDate;

    private Roles role;
}
