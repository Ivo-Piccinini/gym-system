package com.utnGymGroup.gym_system.features.routine;


import com.utnGymGroup.gym_system.common.interfaces.ICreate;
import com.utnGymGroup.gym_system.common.interfaces.IUpdate;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@RequiredArgsConstructor
public class RoutineRequestDto {

    @NotNull(message = "El ID público es obligatorio para actualizar", groups = IUpdate.class)
    private UUID publicId;

    @NotBlank(message = "La rutina no puede no tener nombre", groups = ICreate.class)
    @Size(min = 4, message = "El nombre debe tener al menos 4 caracteres", groups = {ICreate.class, IUpdate.class})
    private String name;


    @NotNull(message = "La rutina debe estar asignada a un cliente", groups = ICreate.class)
    private UUID clientId;

    // Opcional: si no se envía, se asigna el usuario autenticado (profesor/admin que la crea).
    private UUID professorId;

    @NotNull(message = "Debe especificar la fecha de inicio", groups = ICreate.class)
    private LocalDate startDate;

    private LocalDate endDate; // Puede ser opcional si la rutina no tiene fin definido todavía


    @NotNull(message = "Debe seleccionar un tipo de rutina válido", groups = {ICreate.class, IUpdate.class})
    private TypeRoutines typeRoutine;

    // Carga estructurada: ejercicios anidados con series, repeticiones y peso.
    // En alta crea los renglones; en edición sincroniza (null = no modifica los ejercicios).
    @Valid
    private List<RoutineExerciseRequestDto> exercises;
}