package com.utnGymGroup.gym_system.features.GymClass;

import com.utnGymGroup.gym_system.common.interfaces.ICreate;
import com.utnGymGroup.gym_system.common.interfaces.IUpdate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/gym-classes")
@RequiredArgsConstructor
@Tag(name = "Clases Programadas", description = "Endpoints para la gestión de grillas horarias, asignación de profesores y horarios de las clases del gimnasio.")
public class GymClassController {

    private final GymClassService gymClassService;

    @GetMapping
    @Operation(summary = "Listar todas las clases", description = "Retorna el listado completo de la grilla horaria de clases activas del gimnasio.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado de clases obtenido correctamente."),
            @ApiResponse(responseCode = "401", description = "No autenticado.")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR', 'CLIENT')")
    public ResponseEntity<List<GymClassDTO>> getAllGymClasses() {
        return ResponseEntity.ok(gymClassService.getAllClasses());
    }

    @GetMapping("/{externalId}")
    @Operation(summary = "Obtener clase por ID externo", description = "Busca los detalles de una clase específica a través de su UUID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Clase encontrada con éxito."),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "404", description = "La clase no existe o fue dada de baja.")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR', 'CLIENT')")
    public ResponseEntity<GymClassDTO> getGymClassByExternalId(
            @Parameter(description = "UUID de la clase programada", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID externalId) {
        return ResponseEntity.ok(gymClassService.getClassByExternalId(externalId));
    }

    @GetMapping("/professor/{professorId}")
    @Operation(summary = "Filtrar clases por Profesor", description = "Devuelve todas las clases activas asignadas a un profesor en específico.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente.")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR', 'CLIENT')")
    public ResponseEntity<List<GymClassDTO>> getGymClassesByProfessor(
            @Parameter(description = "UUID externo del profesor", example = "123e4567-e89b-12d3-a456-426614174001")
            @PathVariable UUID professorId) {
        return ResponseEntity.ok(gymClassService.getClassesByProfessor(professorId));
    }

    @GetMapping("/activity/{activityId}")
    @Operation(summary = "Filtrar clases por Actividad", description = "Devuelve todas las clases activas que corresponden a una actividad específica (Ej: todas las clases de Crossfit).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente.")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR', 'CLIENT')")
    public ResponseEntity<List<GymClassDTO>> getGymClassesByActivity(
            @Parameter(description = "UUID externo de la actividad", example = "987e6543-e21b-12d3-a456-426614174999")
            @PathVariable UUID activityId) {
        return ResponseEntity.ok(gymClassService.getClassesByActivity(activityId));
    }

    @GetMapping("/day")
    @Operation(summary = "Filtrar clases por Día de la Semana", description = "Permite ver la grilla de horarios filtrada por un día específico (Ej: LUNES, MARTES).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente."),
            @ApiResponse(responseCode = "400", description = "Día de la semana inválido.")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR', 'CLIENT')")
    public ResponseEntity<List<GymClassDTO>> getGymClassesByDay(
            @Parameter(description = "Día de la semana en mayúsculas", example = "LUNES")
            @RequestParam DayOfWeek dayOfWeek) {
        return ResponseEntity.ok(gymClassService.getClassesByDay(dayOfWeek));
    }

    @PostMapping
    @Operation(summary = "Crear una nueva clase horaria", description = "Registra una nueva clase en la grilla. Valida que el profesor no tenga otra clase superpuesta en ese mismo horario y día.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Clase programada con éxito."),
            @ApiResponse(responseCode = "400", description = "Error de validación (Ej: La hora de inicio es posterior a la de fin, o el profesor ya tiene una clase asignada que se solapa en ese horario)."),
            @ApiResponse(responseCode = "403", description = "No autorizado (Solo ADMIN o PROFESSOR).")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<GymClassDTO> createGymClass(
            @Parameter(description = "Datos de la nueva clase a programar")
            @Validated(ICreate.class) @RequestBody GymClassDTO gymClassDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(gymClassService.createClass(gymClassDTO));
    }

    @PutMapping("/{externalId}")
    @Operation(summary = "Actualizar una clase existente", description = "Modifica los datos de una clase programada (horarios, cupo, día). Re-valida posibles solapamientos de horario para el profesor.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Clase actualizada correctamente."),
            @ApiResponse(responseCode = "400", description = "Error de validación (Ej: Solapamiento de horarios detectado al mover la clase)."),
            @ApiResponse(responseCode = "403", description = "No autorizado."),
            @ApiResponse(responseCode = "404", description = "La clase no existe o está inactiva.")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<GymClassDTO> updateGymClass(
            @Parameter(description = "UUID de la clase a modificar")
            @PathVariable UUID externalId,
            @Validated(IUpdate.class) @RequestBody GymClassDTO gymClassDTO) {
        return ResponseEntity.ok(gymClassService.updateClass(externalId, gymClassDTO));
    }

    @DeleteMapping("/{externalId}")
    @Operation(summary = "Eliminar una clase programada (Baja lógica)", description = "Da de baja la clase programada. El sistema rechazará la eliminación si la clase ya tiene alumnos inscriptos.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Clase eliminada con éxito (baja lógica)."),
            @ApiResponse(responseCode = "400", description = "Operación denegada: No se puede eliminar porque la clase tiene alumnos inscriptos (IllegalStateException)."),
            @ApiResponse(responseCode = "403", description = "No autorizado."),
            @ApiResponse(responseCode = "404", description = "La clase no existe.")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<Void> deleteGymClass(
            @Parameter(description = "UUID de la clase a dar de baja")
            @PathVariable UUID externalId) {
        gymClassService.deleteClass(externalId);
        return ResponseEntity.noContent().build();
    }
}