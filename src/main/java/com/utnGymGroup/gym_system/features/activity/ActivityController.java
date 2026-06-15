package com.utnGymGroup.gym_system.features.activity;

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
@RequestMapping("/api/activities")
@RequiredArgsConstructor
@Tag(name = "Actividades", description = "Endpoints para la gestión de las actividades maestras del gimnasio (ej. Crossfit, Zumba, Funcional).")
public class ActivityController {

    private final ActivityService activityService;

    @GetMapping
    @Operation(summary = "Listar todas las actividades", description = "Retorna el listado completo de actividades activas disponibles en el gimnasio.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listado de actividades obtenido correctamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado (Token JWT faltante o inválido)"),
            @ApiResponse(responseCode = "403", description = "No autorizado (Rol insuficiente)")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR', 'CLIENT')")
    public ResponseEntity<List<ActivityDTO>> getAllActivities() {
        return ResponseEntity.ok(activityService.getAllActivities());
    }

    @GetMapping("/{externalId}")
    @Operation(summary = "Obtener actividad por ID externo", description = "Busca una actividad específica y activa a través de su UUID público.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Actividad encontrada exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No autorizado"),
            @ApiResponse(responseCode = "404", description = "Actividad no encontrada o dada de baja previamente")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR', 'CLIENT')")
    public ResponseEntity<ActivityDTO> getActivityByExternalId(
            @Parameter(description = "UUID único identificador de la actividad", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID externalId) {
        return ResponseEntity.ok(activityService.getActivityByExternalId(externalId));
    }

    @PostMapping
    @Operation(summary = "Crear una nueva actividad", description = "Permite registrar una nueva actividad validando que contenga los datos correctos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Actividad creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos (Validación de DTO fallida)"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No autorizado (Solo ADMIN o PROFESSOR pueden crear)")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<ActivityDTO> createActivity(
            @Parameter(description = "Objeto JSON con los datos de la nueva actividad")
            @Validated(ICreate.class) @RequestBody ActivityDTO activityDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(activityService.createActivity(activityDTO));
    }

    @PutMapping("/{externalId}")
    @Operation(summary = "Actualizar una actividad existente", description = "Modifica los datos de una actividad activa buscando por su UUID externo.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Actividad actualizada correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos (Validación de DTO fallida)"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No autorizado (Solo ADMIN o PROFESSOR pueden actualizar)"),
            @ApiResponse(responseCode = "404", description = "Actividad no encontrada o inactiva")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<ActivityDTO> updateActivity(
            @Parameter(description = "UUID de la actividad a actualizar", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID externalId,
            @Parameter(description = "Objeto JSON con los nuevos datos a sobrescribir")
            @Validated(IUpdate.class) @RequestBody ActivityDTO activityDTO) {
        return ResponseEntity.ok(activityService.updateActivity(externalId, activityDTO));
    }

    @DeleteMapping("/{externalId}")
    @Operation(summary = "Eliminar una actividad (Baja Lógica)", description = "Realiza la baja lógica de la actividad (cambia su estado a inactiva) siempre y cuando cumpla con las reglas de negocio.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Actividad dada de baja exitosamente (Sin contenido de retorno)"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No autorizado (Solo ADMIN o PROFESSOR pueden eliminar)"),
            @ApiResponse(responseCode = "404", description = "Actividad no encontrada o ya inactiva")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<Void> deleteActivity(
            @Parameter(description = "UUID de la actividad a dar de baja", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID externalId) {
        activityService.deleteActivity(externalId);
        return ResponseEntity.noContent().build();
    }
}