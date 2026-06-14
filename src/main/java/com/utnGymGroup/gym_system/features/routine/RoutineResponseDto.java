package com.utnGymGroup.gym_system.features.routine;

import com.utnGymGroup.gym_system.features.user.dtos.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoutineResponseDto {
    private UUID publicId;

    private String name;
    private Date startDate;
    private Date endDate;
    private TypeRoutines typeRoutine;

    private UserDTO client;
    private UserDTO professor;
}
