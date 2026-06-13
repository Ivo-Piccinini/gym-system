package com.utnGymGroup.gym_system.features.exercise;

import com.utnGymGroup.gym_system.common.interfaces.ICreate;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


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
    public ResponseEntity<ExerciseDto> createExercise(@Validated(ICreate.class) @RequestBody ExerciseDto exerciseDto, @RequestParam(required = true) String usuarioEmail)
    {
        return ResponseEntity.ok(exerciseService.createExercise(exerciseDto,usuarioEmail));
    }

    @GetMapping("/{publicId}")
    public ResponseEntity<ExerciseDto> getExercise(@PathVariable UUID publicId)
    {
        return ResponseEntity.ok(exerciseService.findByPublicId(publicId));

    }

    @DeleteMapping("/{publicId}/user/{email}")
    public ResponseEntity<Void> deleteExercise(@PathVariable UUID publicId,@PathVariable String email)
    {
         exerciseService.deleteExercise(publicId,email);
        return ResponseEntity.noContent().build();
    }


}
