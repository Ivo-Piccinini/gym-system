package com.utnGymGroup.gym_system.features.audit;

import com.utnGymGroup.gym_system.features.audit.exceptions.AuditSerializationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.Arrays;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditAspect {
    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    // esto intercepta cualquier método anotado con @Auditable una vez que termine exitosamente
    @AfterReturning(pointcut = "@annotation(auditable)", returning = "result")
    public void auditMethod(JoinPoint joinPoint, Auditable auditable, Object result){
        AuditActions actionName = auditable.value();

        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        String detailsJson;
        try {
            detailsJson = objectMapper.writeValueAsString(joinPoint.getArgs());
        } catch (Exception e){
            log.error("Fallo crítico: No se pudo generar el log de auditoría en JSON", e);
            // Lanzamos la excepción personalizada para abortar la transacción de negocio
            throw new AuditSerializationException("Error al serializar detalles de auditoría para la acción: " + actionName, e);
        }

        AuditLogEntity log = AuditLogEntity.builder()
                .action(actionName)
                .performedBy(currentUser)
                .timestamp(LocalDateTime.now())
                .details(detailsJson)
                .build();

        auditLogRepository.save(log);
        System.out.println(">> [AOP AUDIT] Acción '" + actionName + "' auditada con éxito en la base de datos.");
    }
}
