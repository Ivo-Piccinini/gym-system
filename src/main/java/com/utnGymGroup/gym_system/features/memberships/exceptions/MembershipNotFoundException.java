package com.utnGymGroup.gym_system.features.memberships.exceptions;

public class MembershipNotFoundException extends RuntimeException {
    public MembershipNotFoundException(String message) {
        super(message);
    }
}
