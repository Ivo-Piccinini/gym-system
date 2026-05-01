package com.utnGymGroup.gym_system.features.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.utnGymGroup.gym_system.common.interfaces.ICreate;
import com.utnGymGroup.gym_system.common.interfaces.IUpdate;
import com.utnGymGroup.gym_system.features.profile.ProfileDTO;
import com.utnGymGroup.gym_system.features.profile.ProfileEntity;
import com.utnGymGroup.gym_system.features.role.RoleDTO;
import com.utnGymGroup.gym_system.features.role.RoleEntity;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserDTO {
    @NotBlank(message = "El username es requerido", groups = {ICreate.class, IUpdate.class})
    @Size(min = 4, max = 20, message = "El username debe tener entre 4 y 20 caracteres.", groups = {ICreate.class, IUpdate.class})
    private String username;

    @NotBlank(message = "La contraseña es requerida", groups = ICreate.class)
    @Size(min = 8, message = "La contraseña debe tener mínimo 8 caracteres.", groups = {ICreate.class, IUpdate.class})
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) // Se recibe, pero nunca se envía al frontend
    private String password;

    @Email(message = "El email debe ser válido.", groups = {ICreate.class, IUpdate.class})
    @NotBlank(message = "El email es requerido.", groups = ICreate.class)
    private String email;

    private boolean enabled;

    @NotEmpty(message = "Los datos del perfil son obligatorios.", groups = ICreate.class)
    @Valid // IMPORTANTE: Valida los RoleDTO dentro del set
    private Set<RoleDTO> roles = new HashSet<>();

    @NotNull(message = "Los datos del perfil son obligatorios.", groups = ICreate.class)
    @Valid // IMPORTANTE: Valida las anotaciones dentro de ProfileDTO
    private ProfileDTO profile;
}
