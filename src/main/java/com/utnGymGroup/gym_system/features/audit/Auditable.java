package com.utnGymGroup.gym_system.features.audit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD) // para indicar que se va a usar en metodos
@Retention(RetentionPolicy.RUNTIME) // para indicar que va a estar disponible en tiempo de ejecución
public @interface Auditable {
    AuditActions value(); // nombre de la accion
}
