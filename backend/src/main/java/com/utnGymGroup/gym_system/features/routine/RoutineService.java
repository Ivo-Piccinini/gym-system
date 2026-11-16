package com.utnGymGroup.gym_system.features.routine;

import com.utnGymGroup.gym_system.common.auth.credentials.CredentialsEntity;
import com.utnGymGroup.gym_system.common.auth.credentials.CredentialsRepository;
import com.utnGymGroup.gym_system.features.audit.AuditActions;
import com.utnGymGroup.gym_system.features.audit.Auditable;
import com.utnGymGroup.gym_system.features.exercise.ExerciseEntity;
import com.utnGymGroup.gym_system.features.exercise.ExerciseRepository;
import com.utnGymGroup.gym_system.features.exercise.exceptions.ExerciseNotFoundException;
import com.utnGymGroup.gym_system.features.fullRoutine.FullRoutineDtoResponse;
import com.utnGymGroup.gym_system.features.fullRoutine.FullRoutineEntity;
import com.utnGymGroup.gym_system.features.fullRoutine.FullRoutineMapperResponse;
import com.utnGymGroup.gym_system.features.fullRoutine.FullRoutineRepository;
import com.utnGymGroup.gym_system.features.routine.exception.RoutineNotFoundException;
import com.utnGymGroup.gym_system.features.user.UserEntity;
import com.utnGymGroup.gym_system.features.user.UserRepository;
import com.utnGymGroup.gym_system.features.user.exceptions.UserNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoutineService
{
    private final RoutineRepository routineRepository;
    private final RoutineMapperResponse routineMapperResponse;
    private final UserRepository userRepository;
    private final CredentialsRepository credentialsRepository;
    private final FullRoutineRepository fullRoutineRepository;
    private final FullRoutineMapperResponse fullRoutineMapperResponse;
    private final ExerciseRepository exerciseRepository;

    private RoutineResponseDto convertAndPopulateExercises(RoutineEntity routine) {
        RoutineResponseDto dto = routineMapperResponse.convertToDto(routine);
        dto.setTypeRoutine(routine.getType_routine());
        dto.setEnabled(routine.getEnabled());
        List<FullRoutineDtoResponse> exercises = fullRoutineRepository.findAllByRoutinePublicId(routine.getPublicId())
                .stream()
                .map(fullRoutineMapperResponse::convertToDto)
                .toList();
        dto.setExercises(exercises);
        return dto;
    }

    @Transactional
    public List<RoutineResponseDto> getAllRoutines() {
        return routineRepository.findAllByEnabledTrueOrderByStartDateDesc()
                .stream()
                .map(this::convertAndPopulateExercises)
                .toList();
    }

    @Transactional
    public RoutineResponseDto getRoutineByPublicId(UUID publicId) {
        RoutineEntity routine = routineRepository.findByPublicIdAndEnabledTrue(publicId)
                .orElseThrow(() -> new RoutineNotFoundException("No se encontró la rutina con el ID solicitado"));
        return convertAndPopulateExercises(routine);
    }

    @Auditable(AuditActions.CREATE_ROUTINE)
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    @Transactional
    public RoutineResponseDto createRoutine(RoutineRequestDto request) {

        validateDates(request.getStartDate(), request.getEndDate());

        UserEntity client = userRepository.findByPublicId(request.getClientId())
                .orElseThrow(() -> new UserNotFoundException("El cliente especificado no existe"));

        UserEntity professor = (request.getProfessorId() != null)
                ? userRepository.findByPublicId(request.getProfessorId())
                        .orElseThrow(() -> new UserNotFoundException("El profesor especificado no existe"))
                : getAuthenticatedUser();

        RoutineEntity routine = new RoutineEntity();
        routine.setName(request.getName().trim());
        routine.setStartDate(request.getStartDate());
        routine.setEndDate(request.getEndDate());
        routine.setType_routine(request.getTypeRoutine());
        routine.setClient(client);
        routine.setProfessor(professor);
        routine.setEnabled(true);

        RoutineEntity saved = routineRepository.save(routine);
        syncExercises(saved, request.getExercises());
        return convertAndPopulateExercises(saved);
    }

    @Auditable(AuditActions.UPDATE_ROUTINE)
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    @Transactional
    public RoutineResponseDto updateRoutine(RoutineRequestDto request, UUID publicId) {

        RoutineEntity routine = routineRepository.findByPublicIdAndEnabledTrue(publicId)
                .orElseThrow(() -> new RoutineNotFoundException("No se encontró la rutina a modificar"));

        if (request.getClientId() != null
                && (routine.getClient() == null || !routine.getClient().getPublicId().equals(request.getClientId()))) {
            UserEntity newClient = userRepository.findByPublicId(request.getClientId())
                    .orElseThrow(() -> new UserNotFoundException("El nuevo cliente especificado no existe"));
            routine.setClient(newClient);
        }

        if (request.getProfessorId() != null
                && (routine.getProfessor() == null || !routine.getProfessor().getPublicId().equals(request.getProfessorId()))) {
            UserEntity newProfessor = userRepository.findByPublicId(request.getProfessorId())
                    .orElseThrow(() -> new UserNotFoundException("El nuevo profesor especificado no existe"));
            routine.setProfessor(newProfessor);
        }

        if (request.getName() != null) {
            routine.setName(request.getName().trim());
        }
        if (request.getStartDate() != null) {
            routine.setStartDate(request.getStartDate());
        }
        routine.setEndDate(request.getEndDate());
        routine.setType_routine(request.getTypeRoutine());

        validateDates(routine.getStartDate(), routine.getEndDate());

        RoutineEntity saved = routineRepository.save(routine);
        syncExercises(saved, request.getExercises());
        return convertAndPopulateExercises(saved);
    }

    // Baja lógica: se deshabilita la rutina y sus renglones. No se borra nada de la base de datos.
    // Se audita como UPDATE_ROUTINE para no agregar valores nuevos al enum de auditoría (que tiene check constraint en la base).
    @Auditable(AuditActions.UPDATE_ROUTINE)
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    @Transactional
    public void deleteRoutine(UUID publicId) {
        RoutineEntity routine = routineRepository.findByPublicIdAndEnabledTrue(publicId)
                .orElseThrow(() -> new RoutineNotFoundException("No se encontró la rutina a eliminar"));

        routine.setEnabled(false);
        routineRepository.save(routine);

        fullRoutineRepository.findAllByRoutinePublicId(routine.getPublicId()).forEach(line -> {
            line.setEnabled(false);
            fullRoutineRepository.save(line);
        });
    }

    @Transactional
    public RoutineResponseDto getMyRoutine()
    {
        UserEntity client = getAuthenticatedUser();

        RoutineEntity routine = routineRepository.findFirstByClient_PublicIdAndEnabledTrueOrderByIdDesc(client.getPublicId())
                .orElseThrow(() -> new UserNotFoundException("No tenés ninguna rutina asignada actualmente."));

        return convertAndPopulateExercises(routine);
    }

    // ---------------------------------------------------------------- helpers

    private UserEntity getAuthenticatedUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = (principal instanceof UserDetails)
                ? ((UserDetails) principal).getUsername()
                : principal.toString();

        CredentialsEntity credentials = credentialsRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado en el contexto de seguridad"));

        UserEntity user = credentials.getUser();
        if (user == null) {
            throw new UserNotFoundException("El usuario logueado no posee un perfil de cliente configurado");
        }
        return user;
    }

    private void validateDates(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new IllegalStateException("La fecha de fin no puede ser anterior a la fecha de inicio.");
        }
    }

    /**
     * Sincroniza los renglones de la rutina con la lista anidada recibida:
     * - renglón con publicId existente  -> se actualiza
     * - renglón sin publicId            -> se crea
     * - renglón existente no enviado    -> baja lógica
     * Si la lista es null no se toca nada.
     */
    private void syncExercises(RoutineEntity routine, List<RoutineExerciseRequestDto> items) {
        if (items == null) {
            return;
        }

        Map<UUID, FullRoutineEntity> currentById = fullRoutineRepository
                .findAllByRoutinePublicId(routine.getPublicId())
                .stream()
                .collect(Collectors.toMap(FullRoutineEntity::getPublicId, Function.identity()));

        Set<UUID> keep = new HashSet<>();

        for (RoutineExerciseRequestDto item : items) {
            ExerciseEntity exercise = exerciseRepository.findByIdPublic(item.getExerciseId())
                    .orElseThrow(() -> new ExerciseNotFoundException("El ejercicio especificado no existe"));

            FullRoutineEntity line = (item.getPublicId() != null) ? currentById.get(item.getPublicId()) : null;
            boolean isNew = (line == null);

            if (isNew) {
                line = new FullRoutineEntity();
                line.setPublicId(UUID.randomUUID());
                line.setRoutine(routine);
                line.setEnabled(true);
            }

            boolean exerciseChanged = isNew || !line.getExercise().getIdPublic().equals(exercise.getIdPublic());
            if (exerciseChanged && Boolean.FALSE.equals(exercise.getEnabled())) {
                throw new IllegalStateException("El ejercicio \"" + exercise.getName() + "\" está dado de baja y no puede asignarse.");
            }

            line.setExercise(exercise);
            line.setSeries(item.getSeries());
            line.setReps(item.getReps());
            line.setWeight(item.getWeight());
            fullRoutineRepository.save(line);

            keep.add(line.getPublicId());
        }

        currentById.values().stream()
                .filter(line -> !keep.contains(line.getPublicId()))
                .forEach(line -> {
                    line.setEnabled(false);
                    fullRoutineRepository.save(line);
                });
    }
}