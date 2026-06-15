package com.utnGymGroup.gym_system.features.enrollment;

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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
@Tag(name = "Inscripciones", description = "Endpoints para la gestión de inscripciones a clases y control de cupos.")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @GetMapping
    @Operation(
            summary = "Listar todas las inscripciones (Admin/Profesor)",
            description = "Recupera el historial completo de inscripciones del gimnasio. Acceso restringido a administradores y profesores."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado de inscripciones obtenido correctamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado (Token JWT faltante o inválido)"),
            @ApiResponse(responseCode = "403", description = "No autorizado (Solo ADMIN o PROFESSOR tienen acceso a este listado global)")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<List<EnrollmentDTO>> getAllEnrollments() {
        return ResponseEntity.ok(enrollmentService.getAllEnrollments());
    }

    @GetMapping("/my-enrollments")
    @Operation(
            summary = "Ver mis inscripciones (Cliente)",
            description = "Devuelve el listado de las clases a las que se encuentra inscripto el cliente autenticado, extraído de manera segura desde el token JWT."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Inscripciones personales del cliente obtenidas correctamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No autorizado (Solo usuarios con rol CLIENT pueden ver sus inscripciones)")
    })
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<List<EnrollmentDTO>> getMyEnrollments() {
        return ResponseEntity.ok(enrollmentService.getMyEnrollments());
    }

    @PostMapping("/class/{classExternalId}")
    @Operation(
            summary = "Inscribirse en una clase (Cliente)",
            description = "Permite al cliente autenticado anotarse en una clase específica mediante su ID externo. El sistema verificará de forma automática si hay cupos disponibles antes de confirmar la inscripción."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Inscripción realizada con éxito."),
            @ApiResponse(responseCode = "400", description = "Regla de negocio fallida (Ej: Capacidad máxima alcanzada, el cliente ya está inscripto a esa clase, o solapamiento de horarios)."),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No autorizado (Solo los clientes pueden inscribirse)"),
            @ApiResponse(responseCode = "404", description = "La clase solicitada no existe o fue dada de baja.")
    })
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<EnrollmentDTO> enrollClient(
            @Parameter(description = "UUID externo de la clase a la que el cliente desea asistir", example = "123e4567-e89b-12d3-a456-426614174000", required = true)
            @PathVariable UUID classExternalId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(enrollmentService.enrollClient(classExternalId));
    }

    @DeleteMapping("/{externalId}")
    @Operation(
            summary = "Cancelar una inscripción (Cliente)",
            description = "Permite al cliente dar de baja su reserva a una clase antes de que inicie. El sistema valida de forma segura que la inscripción a cancelar realmente pertenezca al usuario logueado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Inscripción cancelada con éxito (Sin contenido de retorno)."),
            @ApiResponse(responseCode = "400", description = "No se puede cancelar (Ej: La clase ya comenzó o no te pertenece)."),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No autorizado (Solo clientes)"),
            @ApiResponse(responseCode = "404", description = "La inscripción no existe o ya fue cancelada.")
    })
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<Void> cancelEnrollment(
            @Parameter(description = "UUID externo de la inscripción específica que se desea cancelar", example = "987e6543-e21b-12d3-a456-426614174999", required = true)
            @PathVariable UUID externalId) {
        enrollmentService.cancelEnrollment(externalId);
        return ResponseEntity.noContent().build();
    }
}