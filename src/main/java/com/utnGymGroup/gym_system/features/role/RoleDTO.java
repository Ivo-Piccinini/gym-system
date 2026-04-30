package com.utnGymGroup.gym_system.features.role;

import com.utnGymGroup.gym_system.common.interfaces.ICreate;
import com.utnGymGroup.gym_system.common.interfaces.IUpdate;
import jakarta.validation.constraints.NotNull;

public class RoleDTO {
    private Long id;
    @NotNull(message = "El nombre del rol es requerido", groups = {ICreate.class, IUpdate.class})
    private Roles name;
}
