package com.utnGymGroup.gym_system.features.fulllRoutine;

import com.utnGymGroup.gym_system.common.interfaces.ICreate;
import com.utnGymGroup.gym_system.common.interfaces.IUpdate;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.UUID;

public class FullRoutineDtoResponse
{
    public UUID publicID;

    private Long exerciseID;

    private Long routineID;

    private Integer series;

    private Integer reps;

    private Integer weight;

}
