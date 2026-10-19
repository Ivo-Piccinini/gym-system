package com.utnGymGroup.gym_system.features.fullRoutine;

import com.utnGymGroup.gym_system.features.exercise.ExerciseDtoResponse;
import com.utnGymGroup.gym_system.features.routine.RoutineRequestDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FullRoutineDtoResponse
{
    private UUID publicId;

    private RoutineRequestDto routine;

    private ExerciseDtoResponse exercise;

    private Integer series;

    private Integer reps;

    private Double weight;
}
