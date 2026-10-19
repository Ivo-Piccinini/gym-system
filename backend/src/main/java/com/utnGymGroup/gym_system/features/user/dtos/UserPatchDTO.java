package com.utnGymGroup.gym_system.features.user.dtos;

import com.utnGymGroup.gym_system.common.interfaces.ICreate;
import com.utnGymGroup.gym_system.common.interfaces.IUpdate;
import jakarta.validation.constraints.Size;
import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Schema(description = "DTO para la actualización parcial del perfil del usuario logueado")
public class UserPatchDTO {
    @Schema(description = "Nombre de pila a modificar", example = "Juan")
    @Size(min = 3, max = 20, message = "El nombre del usuario debe tener entre 3 y 20 caracteres.", groups = {ICreate.class, IUpdate.class})
    private String firstName;

    @Schema(description = "Apellido a modificar", example = "Perez")
    @Size(min = 3, max = 30, message = "El apellido del usuario debe tener entre 3 y 30 caracteres.", groups = {ICreate.class, IUpdate.class})
    private String lastName;

    @Schema(description = "Teléfono de contacto a modificar", example = "3415556677")
    private String phone;

    @Schema(description = "Fecha de nacimiento a modificar", example = "1995-10-15")
    private LocalDate birthDay;
}

