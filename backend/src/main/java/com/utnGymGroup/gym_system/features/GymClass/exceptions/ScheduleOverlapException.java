package com.utnGymGroup.gym_system.features.GymClass.exceptions;

/**
 * Se lanza cuando el profesor ya tiene otra clase asignada en el mismo día y rango horario.
 * El GlobalExceptionHandler la traduce a HTTP 409 (Conflict).
 */
public class ScheduleOverlapException extends RuntimeException {

    public ScheduleOverlapException(String message) {
        super(message);
    }
}
