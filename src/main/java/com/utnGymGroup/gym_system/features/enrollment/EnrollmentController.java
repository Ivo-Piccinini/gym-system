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
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<List<EnrollmentDTO>> getAllEnrollments() {
        return ResponseEntity.ok(enrollmentService.getAllEnrollments());
    }

    @GetMapping("/my-enrollments")
    @Operation(
            summary = "Ver mis inscripciones (Cliente)",
            description = "Devuelve el listado de las clases a las que se encuentra inscripto el cliente autenticado."
    )
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<List<EnrollmentDTO>> getMyEnrollments() {
        return ResponseEntity.ok(enrollmentService.getMyEnrollments());
    }

    @PostMapping("/class/{classExternalId}")
    @Operation(
            summary = "Inscribirse en una clase (Cliente)",
            description = "Permite al cliente autenticado anotarse en una clase específica mediante su ID externo, restando un cupo si hay capacidad disponible."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Inscripción realizada con éxito."),
            @ApiResponse(responseCode = "400", description = "La clase no existe o ya ha alcanzado la capacidad máxima de alumnos para el día de hoy.")
    })
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<EnrollmentDTO> enrollClient(
            @Parameter(description = "UUID externo de la clase programada", required = true)
            @PathVariable UUID classExternalId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(enrollmentService.enrollClient(classExternalId));
    }

    @DeleteMapping("/{externalId}")
    @Operation(
            summary = "Cancelar una inscripción (Cliente)",
            description = "Permite al cliente dar de baja su reserva a una clase antes de que inicie. El sistema valida de forma segura que la inscripción pertenezca al usuario logueado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Inscripción cancelada con éxito (No Content)."),
            @ApiResponse(responseCode = "400", description = "No tienes permisos para cancelar esta inscripción o la misma no existe.")
    })
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<Void> cancelEnrollment(
            @Parameter(description = "UUID externo de la inscripción a eliminar", required = true)
            @PathVariable UUID externalId) {
        enrollmentService.cancelEnrollment(externalId);
        return ResponseEntity.noContent().build();
    }
}