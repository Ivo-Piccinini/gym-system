package com.utnGymGroup.gym_system.features.GymClass;

import com.utnGymGroup.gym_system.common.interfaces.ICreate;
import com.utnGymGroup.gym_system.common.interfaces.IUpdate;
import io.swagger.v3.oas.annotations.Operation;
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
@Tag(name = "Clases Programadas", description = "Endpoints para la gestión de grillas horarias de las clases del gimnasio.")
public class GymClassController {

    private final GymClassService gymClassService;

    @GetMapping
    @Operation(summary = "Listar todas las clases", description = "Retorna el listado completo de la grilla horaria del gimnasio.")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR', 'CLIENT')")
    public ResponseEntity<List<GymClassDTO>> getAllGymClasses() {
        return ResponseEntity.ok(gymClassService.getAllClasses());
    }

    @GetMapping("/{externalId}")
    @Operation(summary = "Obtener clase por ID externo")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR', 'CLIENT')")
    public ResponseEntity<GymClassDTO> getGymClassByExternalId(@PathVariable UUID externalId) {
        return ResponseEntity.ok(gymClassService.getClassByExternalId(externalId));
    }

    @GetMapping("/professor/{professorId}")
    @Operation(summary = "Filtrar clases por Profesor")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR', 'CLIENT')")
    public ResponseEntity<List<GymClassDTO>> getGymClassesByProfessor(@PathVariable UUID professorId) {
        return ResponseEntity.ok(gymClassService.getClassesByProfessor(professorId));
    }

    @GetMapping("/activity/{activityId}")
    @Operation(summary = "Filtrar clases por Actividad")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR', 'CLIENT')")
    public ResponseEntity<List<GymClassDTO>> getGymClassesByActivity(@PathVariable UUID activityId) {
        return ResponseEntity.ok(gymClassService.getClassesByActivity(activityId));
    }

    @GetMapping("/day")
    @Operation(summary = "Filtrar clases por Día de la Semana")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR', 'CLIENT')")
    public ResponseEntity<List<GymClassDTO>> getGymClassesByDay(@RequestParam DayOfWeek dayOfWeek) {
        return ResponseEntity.ok(gymClassService.getClassesByDay(dayOfWeek));
    }

    @PostMapping
    @Operation(summary = "Crear una nueva clase horaria")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Clase programada con éxito."),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o profesor ocupado.")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<GymClassDTO> createGymClass(@Validated(ICreate.class) @RequestBody GymClassDTO gymClassDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(gymClassService.createClass(gymClassDTO));
    }

    @PutMapping("/{externalId}")
    @Operation(summary = "Actualizar una clase existente")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<GymClassDTO> updateGymClass(@PathVariable UUID externalId, @Validated(IUpdate.class) @RequestBody GymClassDTO gymClassDTO) {
        return ResponseEntity.ok(gymClassService.updateClass(externalId, gymClassDTO));
    }

    @DeleteMapping("/{externalId}")
    @Operation(summary = "Eliminar una clase programada")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<Void> deleteGymClass(@PathVariable UUID externalId) {
        gymClassService.deleteClass(externalId);
        return ResponseEntity.noContent().build();
    }
}
