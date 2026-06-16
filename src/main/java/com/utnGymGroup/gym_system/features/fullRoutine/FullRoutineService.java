package com.utnGymGroup.gym_system.features.fullRoutine;

import com.utnGymGroup.gym_system.common.auth.credentials.CredentialsRepository;
import com.utnGymGroup.gym_system.features.audit.AuditActions;
import com.utnGymGroup.gym_system.features.audit.Auditable;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
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
    private final CredentialsRepository credentialsRepository;

    public FullRoutineService(
            FullRoutineRepository fullRoutineRepository, 
            FullRoutineMapperRequest fullRoutineMapperRequest, 
            FullRoutineMapperResponse fullRoutineDtoResponse, 
            RoutineRepository routineRepository, 
            ExerciseRepository exerciseRepository, 
            UserRepository userRepository,
            CredentialsRepository credentialsRepository
    ) {
        this.fullRoutineRepository = fullRoutineRepository;
        this.fullRoutineMapperRequest = fullRoutineMapperRequest;
        this.fullRoutineDtoResponse = fullRoutineDtoResponse;
        this.routineRepository = routineRepository;
        this.exerciseRepository = exerciseRepository;
        this.userRepository = userRepository;
        this.credentialsRepository = credentialsRepository;
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

    private RoutineEntity findRoutineFromRequest(FullRoutineDtoRequest request) {
        RoutineEntity routine = null;
        if (request.getRoutineID() != null) {
            routine = routineRepository.findByPublicId(request.getRoutineID()).orElse(null);
            if (routine == null) {
                routine = routineRepository.findByClient_PublicId(request.getRoutineID()).orElse(null);
            }
        }
        if (routine == null && request.getClientID() != null) {
            routine = routineRepository.findByClient_PublicId(request.getClientID()).orElse(null);
        }
        if (routine == null) {
            throw new RoutineNotFoundException("La rutina especificada o la rutina asociada al cliente no existe");
        }
        return routine;
    }

    @Auditable(AuditActions.ASSIGN_EXERCISE)
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    @Transactional
    public FullRoutineDtoResponse createFullRoutine(FullRoutineDtoRequest request) {

        RoutineEntity routine = findRoutineFromRequest(request);

        if (routine.getClient() == null) {
            throw new IllegalArgumentException("La rutina debe estar asignada a un cliente.");
        }

        // Auto-assign logged-in professor if routine doesn't have one
        if (routine.getProfessor() == null) {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String username = (principal instanceof UserDetails) ?
                    ((UserDetails) principal).getUsername() : principal.toString();
            credentialsRepository.findByUsername(username).ifPresent(creds -> {
                routine.setProfessor(creds.getUser());
                routineRepository.save(routine);
            });
        }

        ExerciseEntity exercise = exerciseRepository.findByIdPublic(request.getExerciseID())
                .orElseThrow(() -> new ExerciseNotFoundException("El ejercicio especificado no existe"));


        FullRoutineEntity entity = new FullRoutineEntity();
        entity.setRoutine(routine);
        entity.setExercise(exercise);
        entity.setSeries(request.getSeries());
        entity.setReps(request.getReps());
        entity.setWeight(request.getWeight());
        entity.setPublicId(UUID.randomUUID());
        entity.setEnabled(true);

        return fullRoutineDtoResponse.convertToDto(fullRoutineRepository.save(entity));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    @Transactional
    public FullRoutineDtoResponse updateFullRoutine(FullRoutineDtoRequest request, UUID publicId) {

        FullRoutineEntity entity = fullRoutineRepository.findByPublicId(publicId)
                .orElseThrow(() -> new FullRoutineNotFound("No se encontró el registro de rutina a modificar"));

        // 2. Si el request intenta cambiar la rutina o el ejercicio, los buscamos y actualizamos
        if (request.getRoutineID() != null || request.getClientID() != null) {
            RoutineEntity newRoutine = findRoutineFromRequest(request);
            if (!entity.getRoutine().getId().equals(newRoutine.getId())) {
                entity.setRoutine(newRoutine);
            }
        }

        if (request.getExerciseID() != null && !entity.getExercise().getIdPublic().equals(request.getExerciseID())) {
            ExerciseEntity newExercise = exerciseRepository.findByIdPublic(request.getExerciseID())
                    .orElseThrow(() -> new ExerciseNotFoundException("El nuevo ejercicio especificado no existe"));
            entity.setExercise(newExercise);
        }

        entity.setSeries(request.getSeries());
        entity.setReps(request.getReps());
        entity.setWeight(request.getWeight());

        return fullRoutineDtoResponse.convertToDto(fullRoutineRepository.save(entity));
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
