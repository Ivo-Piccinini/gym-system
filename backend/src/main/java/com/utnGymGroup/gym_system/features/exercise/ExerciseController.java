package com.utnGymGroup.gym_system.features.exercise;

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
@RequestMapping("/api/exercises")
@RequiredArgsConstructor
public class ExerciseController {

        private final ExerciseService exerciseService;

        // 1. CREAR EJERCICIO
        @PostMapping
        @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
        public ResponseEntity<ExerciseDtoResponse> createExercise(
                @Validated(ICreate.class) @RequestBody ExerciseDtoRequest exerciseDtoRequest) {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(exerciseService.createExercise(exerciseDtoRequest));
        }

        // 2. BUSCAR POR ID PÚBLICO (UUID)
        @GetMapping("/{publicId}")
        @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR', 'CLIENT')")
        public ResponseEntity<ExerciseDtoResponse> getExercise(@PathVariable UUID publicId) {
            return ResponseEntity.ok(exerciseService.findByPublicId(publicId));
        }


        @GetMapping
        @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR', 'CLIENT')")
        public ResponseEntity<List<ExerciseDtoResponse>> getAllExercises(
                @RequestParam(required = false) String muscleGroup) {
            List<ExerciseDtoResponse> exercises = exerciseService.getAllExercises(muscleGroup);
            return ResponseEntity.ok(exercises);
        }


        @PutMapping("/{publicId}")
        @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
        public ResponseEntity<ExerciseDtoResponse> updateExercise(
                @Validated(IUpdate.class) @RequestBody ExerciseDtoRequest exerciseDtoRequest,
                @PathVariable UUID publicId) {
            return ResponseEntity.ok(exerciseService.updateExercise(exerciseDtoRequest, publicId));
        }


        @DeleteMapping("/{publicId}")
        @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
        public ResponseEntity<Void> deleteExercise(@PathVariable UUID publicId) {
            exerciseService.deleteExercise(publicId);
            return ResponseEntity.noContent().build();
        }
    }



