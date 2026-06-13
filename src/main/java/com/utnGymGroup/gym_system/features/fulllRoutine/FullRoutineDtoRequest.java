package com.utnGymGroup.gym_system.features.fulllRoutine;

import com.utnGymGroup.gym_system.common.interfaces.ICreate;
import com.utnGymGroup.gym_system.common.interfaces.IUpdate;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class FullRoutineDtoRequest {
    
    @NotNull(message = "El ejercicio debe tener almenos un id")
    private Long exerciseID;

    @NotNull(message = "La rutina debe tener almenos un id")
    private Long routineID;

    @NotNull(message = "la rutina tiene que tener series" ,groups = ICreate.class)
    @Min(value = 1, groups = {ICreate.class, IUpdate.class})
    private Integer series;

    @NotNull(message = "debe hacer un numero minimo de repeticiones" , groups = ICreate.class)
    @Min(value = 1 , groups = {ICreate.class,IUpdate.class})
    private Integer reps;

    @NotNull(message = "Debe tener un peso minimo el ejercicio")
    @PositiveOrZero
    private Integer weight;
}
