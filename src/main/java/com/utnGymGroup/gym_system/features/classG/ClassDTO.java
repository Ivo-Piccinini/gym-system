package com.utnGymGroup.gym_system.features.classG;

import com.utnGymGroup.gym_system.common.interfaces.ICreate;
import com.utnGymGroup.gym_system.common.interfaces.IUpdate;
import com.utnGymGroup.gym_system.features.activity.ActivityDTO;
import com.utnGymGroup.gym_system.features.user.dtos.UserResponseDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalTime;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter

public class ClassDTO {
    @NotNull(message = "El ID es necesario para actualizar", groups = IUpdate.class)
    private UUID externalId;

    @NotNull(message = "La actividad es requerida", groups = ICreate.class)
    @Valid
    private ActivityDTO activity;

    private String activityName;

    @NotNull(message = "El profesor es requerido", groups = ICreate.class)
    @Valid
    private UserResponseDTO professor;

    @NotNull(message = "El día de la semana es requerido", groups = {ICreate.class, IUpdate.class})
    private DayOfWeek dayOfWeek;

    @NotNull(message = "La hora de inicio es requerida", groups = ICreate.class)
    private LocalTime startTime;

    @NotNull(message = "La hora de fin es requerida", groups = ICreate.class)
    private LocalTime endTime;

    @NotNull(message = "La capacidad máxima es requerida", groups = ICreate.class)
    @Min(value = 1, message = "La capacidad debe ser al menos 1", groups = {ICreate.class, IUpdate.class})
    @Max(value = 50, message = "La capacidad no puede exceder los 50", groups = {ICreate.class, IUpdate.class})
    private Integer capacityMax;
}