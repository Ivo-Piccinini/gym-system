package com.utnGymGroup.gym_system.features.activity;

import com.utnGymGroup.gym_system.common.interfaces.ICreate;
import com.utnGymGroup.gym_system.common.interfaces.IUpdate;
import io.swagger.v3.oas.annotations.Operation;
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
@Tag(name = "Actividades", description = "Endpoints para la gestión de las actividades maestras del gimnasio.")
public class ActivityController {

    private final ActivityService activityService;

    @GetMapping
    @Operation(summary = "Listar todas las actividades", description = "Retorna el listado completo de actividades disponibles en el gimnasio.")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR', 'CLIENT')")
    public ResponseEntity<List<ActivityDTO>> getAllActivities() {
        return ResponseEntity.ok(activityService.getAllActivities());
    }

    @GetMapping("/{externalId}")
    @Operation(summary = "Obtener actividad por ID externo", description = "Busca una actividad específica a través de su UUID público.")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR', 'CLIENT')")
    public ResponseEntity<ActivityDTO> getActivityByExternalId(@PathVariable UUID externalId) {
        return ResponseEntity.ok(activityService.getActivityByExternalId(externalId));
    }

    @PostMapping
    @Operation(summary = "Crear una nueva actividad", description = "Permite registrar una nueva actividad validando que contenga los datos correctos.")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')") // Según RF14 el profesor puede hacer ABM de actividades
    public ResponseEntity<ActivityDTO> createActivity(
            @Validated(ICreate.class) @RequestBody ActivityDTO activityDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(activityService.createActivity(activityDTO));
    }

    @PutMapping("/{externalId}")
    @Operation(summary = "Actualizar una actividad existente", description = "Modifica los datos de una actividad buscando por su UUID externo.")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<ActivityDTO> updateActivity(
            @PathVariable UUID externalId,
            @Validated(IUpdate.class) @RequestBody ActivityDTO activityDTO) {
        return ResponseEntity.ok(activityService.updateActivity(externalId, activityDTO));
    }

    @DeleteMapping("/{externalId}")
    @Operation(summary = "Eliminar una actividad", description = "Realiza la baja física de la actividad siempre y cuando no tenga clases asociadas.")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<Void> deleteActivity(@PathVariable UUID externalId) {
        activityService.deleteActivity(externalId);
        return ResponseEntity.noContent().build();
    }
}