package com.utnGymGroup.gym_system.features.exercise.exceptions;

public class ExerciseNotFoundException extends RuntimeException {
    public ExerciseNotFoundException(String message) {
        super(message);
    }
}
