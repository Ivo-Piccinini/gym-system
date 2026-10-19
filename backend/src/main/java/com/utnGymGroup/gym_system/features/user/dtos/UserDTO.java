package com.utnGymGroup.gym_system.features.user.dtos;

import com.utnGymGroup.gym_system.common.auth.permissions.Roles;
import com.utnGymGroup.gym_system.common.interfaces.ICreate;
import com.utnGymGroup.gym_system.common.interfaces.IUpdate;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Schema(description = "DTO que representa la información completa de la cuenta y perfil de un usuario")
public class UserDTO {

    @Schema(description = "Identificador público del usuario (UUID)", example = "3b29c910-482a-4a27-beaa-f5cf3d99432e")
    @NotNull(message = "El id público es requerido.", groups = IUpdate.class)
    private UUID publicId;

    @Schema(description = "Nombre de pila del usuario", example = "Juan")
    @Size(min = 3, max = 20, message = "El nombre del usuario debe tener entre 3 y 20 caracteres.", groups = {ICreate.class, IUpdate.class})
    private String firstName;

    @Schema(description = "Apellido del usuario", example = "Perez")
    @Size(min = 3, max = 30, message = "El apellido del usuario debe tener entre 3 y 30 caracteres.", groups = {ICreate.class, IUpdate.class})
    private String lastName;

    @Schema(description = "Dirección de correo electrónico único del usuario", example = "juan.perez@gym.com")
    @Email(groups = {ICreate.class, IUpdate.class})
    @NotNull(message = "El email del usuario es requerido.", groups = ICreate.class)
    private String email;

    @Schema(description = "DNI o número de identificación fiscal", example = "10000001")
    @NotNull(message = "El dni del usuario es requerido.", groups = ICreate.class)
    @Size(min = 8, max = 8, message = "El DNI del usuario debe contener 8 caracteres.", groups = {ICreate.class, IUpdate.class})
    private String dni;

    @Schema(description = "Teléfono de contacto del usuario", example = "3415556677")
    private String phone;

    @Schema(description = "Fecha de nacimiento del usuario", example = "1990-05-15")
    private LocalDate birthDate;

    @Schema(description = "Rol asignado al usuario", example = "ROLE_CLIENT")
    private Roles role;
}

