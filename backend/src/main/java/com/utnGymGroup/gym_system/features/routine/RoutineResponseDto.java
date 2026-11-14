package com.utnGymGroup.gym_system.features.routine;

import com.utnGymGroup.gym_system.features.user.dtos.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoutineResponseDto {
    private UUID publicId;

    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private TypeRoutines typeRoutine;
    private Boolean enabled;

    private UserDTO client;
    private UserDTO professor;

    private java.util.List<com.utnGymGroup.gym_system.features.fullRoutine.FullRoutineDtoResponse> exercises;
}