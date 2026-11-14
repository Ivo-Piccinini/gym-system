package com.utnGymGroup.gym_system.features.routine;

import com.utnGymGroup.gym_system.common.interfaces.ICreate;
import com.utnGymGroup.gym_system.common.interfaces.IUpdate;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Renglón anidado de una rutina: un ejercicio maestro con sus series, repeticiones y peso.
 * Se envía dentro de {@link RoutineRequestDto#getExercises()}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoutineExerciseRequestDto {

    // Solo al editar: id público del renglón existente. Si viene vacío se crea un renglón nuevo.
    private UUID publicId;

    @NotNull(message = "Cada renglón debe indicar un ejercicio", groups = {ICreate.class, IUpdate.class})
    private UUID exerciseId;

    @NotNull(message = "Cada ejercicio debe tener series", groups = {ICreate.class, IUpdate.class})
    @Min(value = 1, message = "Las series deben ser al menos 1", groups = {ICreate.class, IUpdate.class})
    private Integer series;

    @NotNull(message = "Cada ejercicio debe tener repeticiones", groups = {ICreate.class, IUpdate.class})
    @Min(value = 1, message = "Las repeticiones deben ser al menos 1", groups = {ICreate.class, IUpdate.class})
    private Integer reps;

    @NotNull(message = "Cada ejercicio debe indicar el peso (0 si es peso corporal)", groups = {ICreate.class, IUpdate.class})
    @PositiveOrZero(message = "El peso no puede ser negativo", groups = {ICreate.class, IUpdate.class})
    private Double weight;
}