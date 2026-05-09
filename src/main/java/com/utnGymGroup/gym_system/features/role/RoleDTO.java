package com.utnGymGroup.gym_system.features.role;

import com.utnGymGroup.gym_system.common.interfaces.ICreate;
import com.utnGymGroup.gym_system.common.interfaces.IUpdate;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class RoleDTO {

    @NotNull(message = "El id público es necesario para actualizar", groups = IUpdate.class)
    private UUID publicId; // id publico

    @NotNull(message = "El nombre es requerido para crear.", groups = ICreate.class)
    private String nombre;
}
