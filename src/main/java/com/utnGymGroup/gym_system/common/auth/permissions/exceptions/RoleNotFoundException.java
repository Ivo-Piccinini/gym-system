package com.utnGymGroup.gym_system.common.auth.permissions.exceptions;

public class RoleNotFoundException extends RuntimeException {
    public RoleNotFoundException(String message) {
        super(message);
    }
}
