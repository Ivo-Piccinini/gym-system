package com.utnGymGroup.gym_system.features.audit;

import com.utnGymGroup.gym_system.features.audit.dtos.AuditLogResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
@Tag(name = "Auditoría", description = "Controlador para la consulta y trazabilidad de logs de auditoría forense (AOP).")
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping("/logs")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Consultar bitácora de auditoría", description = "Devuelve el historial cronológico descendente de eventos auditados por el aspecto AOP. Permitido únicamente para ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Logs de auditoría recuperados exitosamente."),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "Acceso denegado (requiere rol ADMIN).")
    })
    public ResponseEntity<List<AuditLogResponseDto>> getAuditLogs() {
        return ResponseEntity.ok(auditLogService.getAllAuditLogs());
    }
}
