package com.utnGymGroup.gym_system.features.enrollment;


import com.utnGymGroup.gym_system.common.interfaces.ICreate;
import com.utnGymGroup.gym_system.common.interfaces.IUpdate;
import com.utnGymGroup.gym_system.features.classG.ClassDTO;
import com.utnGymGroup.gym_system.features.user.dtos.UserDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder

public class EnrollmentDTO {
    @NotNull(message = "El ID es necesario para actualizar", groups = IUpdate.class)
    private UUID externalId;

    @NotNull(message = "El cliente es obligatorio", groups = ICreate.class)
    @Valid
    private UserDTO client;

    @NotNull(message = "La clase es obligatoria", groups = ICreate.class)
    @Valid
    private ClassDTO gymClass;

    @NotNull(message = "La fecha de inscripción es obligatoria", groups = ICreate.class)
    @PastOrPresent(message = "La fecha de inscripción no puede ser futura", groups = {ICreate.class, IUpdate.class})
    private LocalDate enrollmentDate;


}
