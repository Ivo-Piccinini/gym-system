package com.utnGymGroup.gym_system.features.routine;


import com.utnGymGroup.gym_system.common.interfaces.ICreate;
import com.utnGymGroup.gym_system.common.interfaces.IUpdate;
import com.utnGymGroup.gym_system.features.user.UserDTO;
import com.utnGymGroup.gym_system.features.user.UserEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Date;
import java.util.UUID;

public class RoutineDto {

    private UUID publicID;

    @NotBlank(message = "La rutina no puede no tener nombre" , groups = ICreate.class)
    @Size(min=4, groups = {ICreate.class, IUpdate.class})
    private String name;

    private Long clientID;
    private UserEntity professorID;

    private Date start_date;
    private Date end_date;

    @NotBlank(message = "Debe tener un tipo de rutina", groups = ICreate.class)
    private TypeRoutines type_routine;



}
