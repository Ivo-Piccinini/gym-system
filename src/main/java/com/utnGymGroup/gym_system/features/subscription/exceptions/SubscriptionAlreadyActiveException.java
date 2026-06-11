package com.utnGymGroup.gym_system.features.subscription.exceptions;

public class SubscriptionAlreadyActiveException extends RuntimeException {
    public SubscriptionAlreadyActiveException(String message) {
        super(message);
    }
}
