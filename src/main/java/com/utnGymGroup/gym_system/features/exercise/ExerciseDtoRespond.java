package com.utnGymGroup.gym_system.features.exercise;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

public class ExerciseDtoRespond
{
    private String idPublic;
    private String name;
    private String descripcion;
    private MuscleGroup muscleGroup;
}
