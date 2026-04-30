package com.utnGymGroup.gym_system.features.exercise;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ExerciseDto {

    private String name;
    private String descripcion;
    private String muscle_group;
}
