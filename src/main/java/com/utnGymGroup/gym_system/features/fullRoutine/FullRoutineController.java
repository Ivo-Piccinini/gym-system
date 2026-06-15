package com.utnGymGroup.gym_system.features.fullRoutine;

import com.utnGymGroup.gym_system.common.interfaces.ICreate;
import com.utnGymGroup.gym_system.common.interfaces.IUpdate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/full-routines")
@RequiredArgsConstructor
public class FullRoutineController {

    private final FullRoutineService fullRoutineService;


    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<FullRoutineDtoResponse> createFullRoutine(
            @Validated(ICreate.class) @RequestBody FullRoutineDtoRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(fullRoutineService.createFullRoutine(request));
    }


    @PutMapping("/{publicId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<FullRoutineDtoResponse> updateFullRoutine(
            @PathVariable UUID publicId,
            @Validated(IUpdate.class) @RequestBody FullRoutineDtoRequest request) {

        return ResponseEntity.ok(fullRoutineService.updateFullRoutine(request,publicId));
    }


    @DeleteMapping("/{publicId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<Void> deleteFullRoutine(@PathVariable UUID publicId) {
        fullRoutineService.deleteFullRoutine(publicId);
        return ResponseEntity.noContent().build(); // Devuelve 204 No Content
    }


    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<List<FullRoutineDtoResponse>> getAllFullRoutines() {
        return ResponseEntity.ok(fullRoutineService.getAll());
    }

    // 5. OBTENER UN RENGLÓN ESPECÍFICO POR SU ID PÚBLICO
    @GetMapping("/{publicId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<FullRoutineDtoResponse> getFullRoutineByPublicId(@PathVariable UUID publicId) {
        return ResponseEntity.ok(fullRoutineService.findByPublicId(publicId));
    }
}