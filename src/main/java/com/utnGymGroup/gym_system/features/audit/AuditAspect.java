package com.utnGymGroup.gym_system.features.audit;

import org.aspectj.lang.JoinPoint;

import java.time.LocalDateTime;
import java.util.Arrays;

public class AuditAspect {
    private final AuditLogRepository auditLogRepository;

    public AuditAspect(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    // esto intercepta cualquier método anotado con @Auditable una vez que termine exitosamente
    public void auditMethod(JoinPoint joinPoint, Auditable auditable, Object result){
        AuditActions actionName = auditable.value();

        // TODO: cuando integremos Spring Security, podrán obtener al usuario autenticado dinámicamente con:
        // String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        String currentUser = "SYSTEM_MOCK";
        String details = "Método: " + joinPoint.getSignature().getName() + " | Argumentos enviados: " + Arrays.toString(joinPoint.getArgs());

        AuditLogEntity log = AuditLogEntity.builder()
                .action(actionName)
                .performedBy(currentUser)
                .timestamp(LocalDateTime.now())
                .details(details)
                .build();

        auditLogRepository.save(log);
        System.out.println(">> [AOP AUDIT] Acción '" + actionName + "' auditada con éxito en la base de datos.");
    }
}
