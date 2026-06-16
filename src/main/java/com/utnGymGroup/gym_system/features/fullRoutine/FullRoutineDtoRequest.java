package com.utnGymGroup.gym_system.features.fullRoutine;

import com.utnGymGroup.gym_system.common.interfaces.ICreate;
import com.utnGymGroup.gym_system.common.interfaces.IUpdate;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.UUID;


@Data
@RequiredArgsConstructor
public class FullRoutineDtoRequest {


    private UUID publicId;

    @NotNull(message = "El ejercicio debe tener almenos un id")
    private UUID exerciseID;

    private UUID routineID;

    private UUID clientID;

    @NotNull(message = "la rutina tiene que tener series" ,groups = ICreate.class)
    @Min(value = 1, groups = {ICreate.class, IUpdate.class})
    private Integer series;

    @NotNull(message = "debe hacer un numero minimo de repeticiones" , groups = ICreate.class)
    @Min(value = 1 , groups = {ICreate.class,IUpdate.class})
    private Integer reps;

    @NotNull(message = "el ejercicio debe contar con peso")
    @PositiveOrZero
    private Double weight;

}
