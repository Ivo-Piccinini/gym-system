package com.utnGymGroup.gym_system.features.profile;

import com.utnGymGroup.gym_system.common.interfaces.ICreate;
import com.utnGymGroup.gym_system.common.interfaces.IUpdate;
import com.utnGymGroup.gym_system.features.user.UserDTO;
import com.utnGymGroup.gym_system.features.user.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.PrePersist;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ProfileDTO {
    @NotNull(message = "Las credenciales son obligatorias.", groups = {IUpdate.class, ICreate.class})
    @Valid
    private UserDTO user;

    @NotBlank(message = "El dni es requerido.", groups = {IUpdate.class, ICreate.class})
    @Size(min = 8, max = 8, message = "El dni debe tener 8 caracteres.", groups = {ICreate.class, IUpdate.class})
    @Positive(message = "El dni debe ser un número positivo.", groups = {ICreate.class, IUpdate.class})
    private String dni; // id publico

    @Size(min = 3, message = "El nombre debe tener mínimo 3 caracteres.", groups = {ICreate.class, IUpdate.class})
    private String firstName;

    @Size(min = 3, message = "El apellido debe tener mínimo 3 caracteres.", groups = {ICreate.class, IUpdate.class})
    private String lastName;

    @NotBlank(message = "El numero de teléfono es requerido.", groups = {ICreate.class})
    @Size(min = 10, max = 10, message = "El número de teléfono debe tener 8 caracteres.", groups = {ICreate.class, IUpdate.class})
    @Positive(message = "El número de teléfono debe ser un número positivo.", groups = {ICreate.class, IUpdate.class})
    private String phone;

    private LocalDate birthDate;
}
