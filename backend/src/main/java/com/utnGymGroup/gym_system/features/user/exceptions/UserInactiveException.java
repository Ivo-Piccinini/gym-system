package com.utnGymGroup.gym_system.features.user.exceptions;

public class UserInactiveException extends RuntimeException {
    public UserInactiveException(String message) {
        super(message);
    }
}
