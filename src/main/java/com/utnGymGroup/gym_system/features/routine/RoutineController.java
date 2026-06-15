package com.utnGymGroup.gym_system.features.routine;


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
@RequestMapping("api/routines")
@RequiredArgsConstructor
public class RoutineController
{
    private final RoutineService routineService;

    @GetMapping
    public ResponseEntity<List<RoutineResponseDto>> getAll() {
        return ResponseEntity.ok(routineService.getAllRoutines());
    }

    @GetMapping("/{publicId}")
    public ResponseEntity<RoutineResponseDto> getByPublicId(@PathVariable UUID publicId) {
        return ResponseEntity.ok(routineService.getRoutineByPublicId(publicId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<RoutineResponseDto> create(
            @Validated(ICreate.class) @RequestBody RoutineRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(routineService.createRoutine(request));
    }

    @PutMapping("/{publicId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<RoutineResponseDto> update(
            @Validated(IUpdate.class) @RequestBody RoutineRequestDto request,
            @PathVariable UUID publicId) {
        return ResponseEntity.ok(routineService.updateRoutine(request, publicId));
    }

    @DeleteMapping("/{publicId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<Void> delete(@PathVariable UUID publicId) {
        routineService.deleteRoutine(publicId);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    @GetMapping("/my-routine")
    @PreAuthorize("hasRole('CLIENT')") //
    public ResponseEntity<RoutineResponseDto> getMyRoutine(@PathVariable UUID publidId) {
        return ResponseEntity.ok(routineService.getMyRoutine(publidId));
    }


}
