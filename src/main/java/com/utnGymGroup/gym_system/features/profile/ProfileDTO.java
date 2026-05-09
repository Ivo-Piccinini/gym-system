package com.utnGymGroup.gym_system.features.profile;

import com.utnGymGroup.gym_system.common.interfaces.ICreate;
import com.utnGymGroup.gym_system.common.interfaces.IUpdate;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ProfileDTO {
    @NotBlank(message = "El dni es requerido.", groups = {IUpdate.class, ICreate.class})
    @Size(min = 8, max = 8, message = "El dni debe tener 8 caracteres.", groups = {ICreate.class, IUpdate.class})
    private String dni; // id publico

    @Size(min = 3, message = "El nombre debe tener mínimo 3 caracteres.", groups = {ICreate.class, IUpdate.class})
    private String firstName;

    @Size(min = 3, message = "El apellido debe tener mínimo 3 caracteres.", groups = {ICreate.class, IUpdate.class})
    private String lastName;

    @NotBlank(message = "El numero de teléfono es requerido.", groups = {ICreate.class})
    @Size(min = 10, max = 10, message = "El número de teléfono debe tener 10 caracteres.", groups = {ICreate.class, IUpdate.class})
    private String phone;

    @Past(message = "La fecha de nacimiento debe ser anterior a hoy.", groups = {ICreate.class, IUpdate.class})
    private LocalDate birthDate;
}
