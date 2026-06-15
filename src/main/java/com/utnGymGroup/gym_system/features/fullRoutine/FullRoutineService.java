package com.utnGymGroup.gym_system.features.fullRoutine;

import com.utnGymGroup.gym_system.features.exercise.ExerciseEntity;
import com.utnGymGroup.gym_system.features.exercise.ExerciseRepository;
import com.utnGymGroup.gym_system.features.exercise.exceptions.ExerciseNotFoundException;
import com.utnGymGroup.gym_system.features.fullRoutine.exception.FullRoutineNotFound;
import com.utnGymGroup.gym_system.features.routine.RoutineEntity;
import com.utnGymGroup.gym_system.features.routine.RoutineRepository;
import com.utnGymGroup.gym_system.features.routine.exception.RoutineNotFoundException;
import com.utnGymGroup.gym_system.features.user.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class FullRoutineService
{
    private final FullRoutineRepository fullRoutineRepository;
    private final FullRoutineMapperRequest fullRoutineMapperRequest;
    private final FullRoutineMapperResponse fullRoutineDtoResponse;
    private final RoutineRepository routineRepository;
    private final ExerciseRepository exerciseRepository;

    private final UserRepository userRepository;

    public FullRoutineService(FullRoutineRepository fullRoutineRepository, FullRoutineMapperRequest fullRoutineMapperRequest, FullRoutineMapperResponse fullRoutineDtoResponse, RoutineRepository routineRepository, ExerciseRepository exerciseRepository, UserRepository userRepository) {
        this.fullRoutineRepository = fullRoutineRepository;
        this.fullRoutineMapperRequest = fullRoutineMapperRequest;
        this.fullRoutineDtoResponse = fullRoutineDtoResponse;
        this.routineRepository = routineRepository;
        this.exerciseRepository = exerciseRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public List<FullRoutineDtoResponse> getAll() {
        return fullRoutineRepository.findAll()
                .stream()
                .map(fullRoutineDtoResponse :: convertToDto)
                .toList();
    }

    @Transactional
    public FullRoutineDtoResponse findByPublicId(UUID publicId) {
        FullRoutineEntity entity = fullRoutineRepository.findByPublicId(publicId)
                .orElseThrow(() -> new FullRoutineNotFound("No se encontró el renglón de rutina solicitado"));
        return fullRoutineDtoResponse.convertToDto(entity);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    @Transactional
    public FullRoutineDtoResponse createFullRoutine(FullRoutineDtoRequest request) {

        RoutineEntity routine = routineRepository.findByPublicId(request.getPublicId())
                .orElseThrow(() -> new RoutineNotFoundException("La rutina especificada no existe"));


        ExerciseEntity exercise = exerciseRepository.findByIdPublic(request.getPublicId())
                .orElseThrow(() -> new ExerciseNotFoundException("El ejercicio especificado no existe"));


        FullRoutineEntity entity = new FullRoutineEntity();
        entity.setRoutine(routine);
        entity.setExercise(exercise);
        entity.setSeries(request.getSeries());
        entity.setReps(request.getReps());
        entity.setWeight(request.getWeight());
        entity.setPublicId(UUID.randomUUID());
        entity.setEnabled(true);

        return fullRoutineDtoResponse.convertToDto(entity);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    @Transactional
    public FullRoutineDtoResponse updateFullRoutine(FullRoutineDtoRequest request, UUID publicId) {

        FullRoutineEntity entity = fullRoutineRepository.findByPublicId(publicId)
                .orElseThrow(() -> new FullRoutineNotFound("No se encontró el registro de rutina a modificar"));

        // 2. Si el request intenta cambiar la rutina o el ejercicio, los buscamos y actualizamos
        if (!entity.getRoutine().getPublicId().equals(request.getRoutineID())) {
            RoutineEntity newRoutine = routineRepository.findByPublicId(request.getPublicId())
                    .orElseThrow(() -> new RoutineNotFoundException("La nueva rutina especificada no existe"));
            entity.setRoutine(newRoutine);
        }

        if (!entity.getExercise().getIdPublic().equals(request.getExerciseID())) {
            ExerciseEntity newExercise = exerciseRepository.findByIdPublic(request.getPublicId())
                    .orElseThrow(() -> new ExerciseNotFoundException("El nuevo ejercicio especificado no existe"));
            entity.setExercise(newExercise);
        }

        entity.setSeries(request.getSeries());
        entity.setReps(request.getReps());
        entity.setWeight(request.getWeight());

        return fullRoutineDtoResponse.convertToDto(entity);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    @Transactional
    public void deleteFullRoutine(UUID publicId) {
        FullRoutineEntity entity = fullRoutineRepository.findByPublicId(publicId)
                .orElseThrow(() -> new FullRoutineNotFound("No se encontró el registro a eliminar"));

        entity.setEnabled(false);
        fullRoutineRepository.save(entity);
    }



}
