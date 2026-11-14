package com.utnGymGroup.gym_system.features.access;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/access")
@RequiredArgsConstructor
@Tag(name = "Control de Acceso y Credenciales QR", description = "Endpoints para la generación y consulta de pases QR de acceso a sedes.")
public class AccessPassController {

    private final AccessPassService accessPassService;

    @PostMapping("/generate/{userPublicId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Generar pase QR de acceso (Admin)",
            description = "Genera una credencial de acceso con código QR para un socio específico con membresía activa y pago verificado. Exclusivo para ADMIN."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Pase QR generado exitosamente."),
            @ApiResponse(responseCode = "400", description = "Regla de negocio fallida (ej. el socio no posee membresía activa o pago aprobado)."),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado (Solo administradores)."),
            @ApiResponse(responseCode = "404", description = "Socio no encontrado.")
    })
    public ResponseEntity<AccessPassDTO> generateAccessPass(
            @Parameter(description = "UUID público del socio", required = true)
            @PathVariable UUID userPublicId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accessPassService.generateAccessPass(userPublicId));
    }

    @GetMapping("/user/{userPublicId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Consultar pase QR de un socio (Admin)",
            description = "Obtiene el pase QR de acceso activo del socio especificado. Exclusivo para ADMIN."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pase QR obtenido exitosamente."),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado."),
            @ApiResponse(responseCode = "404", description = "El socio no posee pase activo emitido o no existe.")
    })
    public ResponseEntity<AccessPassDTO> getActivePassByUser(
            @Parameter(description = "UUID público del socio", required = true)
            @PathVariable UUID userPublicId) {
        return ResponseEntity.ok(accessPassService.getActivePassByUser(userPublicId));
    }

    @GetMapping("/my-pass")
    @PreAuthorize("hasRole('CLIENT') and !hasRole('ADMIN')")
    @Operation(
            summary = "Consultar mi pase QR de acceso (Cliente)",
            description = "Devuelve el pase digital con código QR de acceso a sedes del socio autenticado para escanear en molinetes."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pase QR personal obtenido correctamente."),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado (Solo socios)."),
            @ApiResponse(responseCode = "404", description = "No posee un pase QR activo emitido.")
    })
    public ResponseEntity<AccessPassDTO> getMyActivePass() {
        return ResponseEntity.ok(accessPassService.getMyActivePass());
    }
}
