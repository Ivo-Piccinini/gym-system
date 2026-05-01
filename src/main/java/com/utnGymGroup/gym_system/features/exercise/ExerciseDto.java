package com.utnGymGroup.gym_system.features.exercise;


import com.utnGymGroup.gym_system.common.interfaces.ICreate;
import com.utnGymGroup.gym_system.common.interfaces.IUpdate;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ExerciseDto {


    @NotBlank
   private String idPublic;


    @NotBlank(message = "El ejercicio debe tener un nombre", groups = ICreate.class)
    @Size(min = 3, groups = {ICreate.class, IUpdate.class})
    private String name;

    private String descripcion;
    private String muscle_group;
}
