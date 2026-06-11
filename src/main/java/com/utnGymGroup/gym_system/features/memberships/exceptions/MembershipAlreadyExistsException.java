package com.utnGymGroup.gym_system.features.memberships.exceptions;

public class MembershipAlreadyExistsException extends RuntimeException{

    public MembershipAlreadyExistsException (String message){
        super(message);
    }
}
