package com.utnGymGroup.gym_system.features.user.exceptions;

public class UsernameChangeNotAllowedException extends RuntimeException {
    public UsernameChangeNotAllowedException(String message) {
        super(message);
    }
}
