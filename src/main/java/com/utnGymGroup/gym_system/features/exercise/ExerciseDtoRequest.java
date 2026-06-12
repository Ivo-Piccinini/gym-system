package com.utnGymGroup.gym_system.features.exercise;


import com.utnGymGroup.gym_system.common.interfaces.ICreate;
import com.utnGymGroup.gym_system.common.interfaces.IUpdate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class ExerciseDtoRequest {

    @NotBlank(message = "El ejercicio debe tener un nombre", groups = ICreate.class)
    @Size(min = 3, groups = {ICreate.class, IUpdate.class})
    private String name;

    @NotBlank(message = "Debe tener explicacion minima del ejercicio ", groups = {ICreate.class, IUpdate.class})
    @Size(min = 10, groups = {ICreate.class,IUpdate.class})
    private String descripcion;

    @NotBlank(groups = {ICreate.class})
    private String muscle_group;

    @NotNull(groups = {ICreate.class, IUpdate.class})
    private Boolean enabled;
}
