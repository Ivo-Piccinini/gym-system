package com.utnGymGroup.gym_system.features.exercise;

import com.utnGymGroup.gym_system.common.interfaces.ICreate;
import com.utnGymGroup.gym_system.common.interfaces.IUpdate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/exercises")
@RequiredArgsConstructor
public class ExerciseController {

    private final ExerciseService exerciseService;

    //Valid no va para get , solo post  y put, y solo va delante de los @
    /*@GetMapping("/{id}")
    public ResponseEntity<ExerciseDto> getExercise(@PathVariable String publicId)
    {
        return ResponseEntity.ok(exerciseService.fi);
    }*/

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<ExerciseDtoResponse> createExercise(@Validated(ICreate.class) @RequestBody ExerciseDtoRequest exerciseDtoRequest)
    {
        return ResponseEntity.status(HttpStatus.CREATED).body(exerciseService.createExercise(exerciseDtoRequest));
    }

    @GetMapping("/{publicId}")
    public ResponseEntity<ExerciseDtoResponse> getExercise(@PathVariable Long publicId)
    {
        return ResponseEntity.ok(exerciseService.findByPublicId(publicId));

    }

    @DeleteMapping("/{publidId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<Void> deleteExercise(@PathVariable Long publicId)
    {
         exerciseService.deleteExercise(publicId);
        return ResponseEntity.noContent().build();
    }


    @PutMapping("/{publicId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<ExerciseDtoResponse> updateExercise(@Validated(IUpdate.class) @RequestBody ExerciseDtoRequest exerciseDtoRequest , @PathVariable Long publicId)
    {
       return ResponseEntity.ok(exerciseService.updateExercise(exerciseDtoRequest,publicId));
    }


}
