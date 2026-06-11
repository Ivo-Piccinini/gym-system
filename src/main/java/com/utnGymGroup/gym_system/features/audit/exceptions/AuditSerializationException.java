package com.utnGymGroup.gym_system.features.audit.exceptions;

public class AuditSerializationException extends RuntimeException {
    
    public AuditSerializationException(String message) {
        super(message);
    }

    public AuditSerializationException(String message, Throwable cause) {
        super(message, cause);
    }}
