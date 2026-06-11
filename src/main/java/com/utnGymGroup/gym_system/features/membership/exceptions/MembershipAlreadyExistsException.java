package com.utnGymGroup.gym_system.features.membership.exceptions;

public class MembershipAlreadyExistsException extends RuntimeException{

    public MembershipAlreadyExistsException (String message){
        super(message);
    }
}
