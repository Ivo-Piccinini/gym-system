package com.utnGymGroup.gym_system.features.audit;

public enum AuditActions {
    // Autenticación y Cuentas de Usuario
    USER_REGISTRATION,
    LOGIN,
    LOGOUT,
    CHANGE_PASSWORD,
    UPDATE_PROFILE,
    TOGGLE_USER_STATUS,
    // Planes y Suscripciones
    CREATE_PLAN,
    UPDATE_PLAN,
    DELETE_PLAN,
    CREATE_SUBSCRIPTION,
    CANCEL_SUBSCRIPTION,
    // Transacciones de Pago
    PROCESS_PAYMENT,
    // Gestión de Clases y Actividades
    CREATE_ACTIVITY,
    UPDATE_ACTIVITY,
    DELETE_ACTIVITY,
    CREATE_CLASS,
    UPDATE_CLASS,
    DELETE_CLASS,
    // Inscripciones a Clases
    ENROLL_IN_CLASS,
    CANCEL_ENROLLMENT,
    // Gestión de Rutinas y Ejercicios
    CREATE_EXERCISE,
    UPDATE_EXERCISE,
    DELETE_EXERCISE,
    CREATE_ROUTINE,
    UPDATE_ROUTINE,
    ASSIGN_EXERCISE
}
