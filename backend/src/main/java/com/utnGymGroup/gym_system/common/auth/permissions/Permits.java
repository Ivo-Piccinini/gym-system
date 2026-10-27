package com.utnGymGroup.gym_system.common.auth.permissions;

public enum Permits {
    // --- GESTIÓN DE USUARIOS Y PERFILES (user / profile) ---
    USER_CREATE,
    USER_READ,
    USER_UPDATE,
    USER_DELETE,
    USER_MANAGE_STATUS,    // Activar/desactivar usuarios (toggleStatus)

    // --- ACTIVIDADES (activity) ---
    ACTIVITY_CREATE,
    ACTIVITY_READ,
    ACTIVITY_UPDATE,
    ACTIVITY_DELETE,

    // --- CLASES PROGRAMADAS (classG) ---
    CLASS_CREATE,
    CLASS_READ,
    CLASS_UPDATE,
    CLASS_DELETE,

    // --- INSCRIPCIONES A CLASES (enrollment) ---
    ENROLLMENT_CREATE,
    ENROLLMENT_READ,
    ENROLLMENT_UPDATE,
    ENROLLMENT_DELETE,

    // --- EJERCICIOS Y RUTINAS (exercise / routine) ---
    EXERCISE_CREATE,
    EXERCISE_READ,
    EXERCISE_UPDATE,
    EXERCISE_DELETE,
    
    ROUTINE_CREATE,
    ROUTINE_READ,
    ROUTINE_UPDATE,
    ROUTINE_DELETE,

    // --- PLANES DE MEMBRESÍA Y SUSCRIPCIONES (memberships / subscription) ---
    MEMBERSHIP_CREATE,
    MEMBERSHIP_READ,
    MEMBERSHIP_UPDATE,
    MEMBERSHIP_DELETE,
    
    SUBSCRIPTION_CREATE,
    SUBSCRIPTION_READ,
    SUBSCRIPTION_UPDATE,
    SUBSCRIPTION_DELETE,

    // --- PAGOS (payments) ---
    PAYMENT_CREATE,
    PAYMENT_READ,
    PAYMENT_UPDATE,

    // --- AUDITORÍA (audit) ---
    AUDIT_READ
}
