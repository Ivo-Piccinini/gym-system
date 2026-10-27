package com.utnGymGroup.gym_system.features.exercise.exceptions;

import org.springframework.web.bind.annotation.ExceptionHandler;


public class ExerciseAlreadyExistsException extends RuntimeException
{
    public ExerciseAlreadyExistsException(String message) {
        super(message);
    }
}
