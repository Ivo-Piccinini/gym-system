package com.utnGymGroup.gym_system.features.routine;

import com.utnGymGroup.gym_system.features.audit.AuditActions;
import com.utnGymGroup.gym_system.features.audit.Auditable;
import com.utnGymGroup.gym_system.features.routine.exception.RoutineNotFoundException;
import com.utnGymGroup.gym_system.features.user.UserEntity;
import com.utnGymGroup.gym_system.features.user.UserRepository;
import com.utnGymGroup.gym_system.features.user.exceptions.UserNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class RoutineService
{
    private final RoutineRepository routineRepository;
    private final RoutineMapperRequest routineMapperRequest;
    private final RoutineMapperResponse routineMapperResponse;

    private final UserRepository userRepository;

    public RoutineService(RoutineRepository routineRepository, RoutineMapperRequest routineMapperRequest, RoutineMapperResponse routineMapperResponse, UserRepository userRepository) {
        this.routineRepository = routineRepository;
        this.routineMapperRequest = routineMapperRequest;
        this.routineMapperResponse = routineMapperResponse;
        this.userRepository = userRepository;
    }

    @Transactional
    public List<RoutineResponseDto> getAllRoutines() {
        return routineRepository.findAll()
                .stream()
                .map(routineMapperResponse::convertToDto)
                .toList();
    }

    @Transactional
    public RoutineResponseDto getRoutineByPublicId(UUID publicId) {
        RoutineEntity routine = routineRepository.findByPublicId(publicId)
                .orElseThrow(() -> new RoutineNotFoundException("No se encontró la rutina con el ID solicitado"));
        return routineMapperResponse.convertToDto(routine);
    }

    @Auditable(AuditActions.CREATE_ROUTINE)
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    @Transactional
    public RoutineResponseDto createRoutine(RoutineRequestDto request) {

        UserEntity client = userRepository.findByPublicId(request.getClientId())
                .orElseThrow(() -> new UserNotFoundException("El cliente especificado no existe"));

        UserEntity professor = userRepository.findByPublicId(request.getProfessorId())
                .orElseThrow(() -> new UserNotFoundException("El profesor especificado no existe"));


        RoutineEntity routineEntity = routineMapperRequest.convertToEntity(request);

        routineEntity.setClient(client);
        routineEntity.setProfessor(professor);

        return routineMapperResponse.convertToDto(routineRepository.save(routineEntity));
    }

    @Auditable(AuditActions.UPDATE_ROUTINE)
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    @Transactional
    public RoutineResponseDto updateRoutine(RoutineRequestDto request, UUID publicId) {

        RoutineEntity routineEntity = routineRepository.findByPublicId(publicId)
                .orElseThrow(() -> new RoutineNotFoundException("No se encontró la rutina a modificar"));


        if (!routineEntity.getClient().getPublicId().equals(request.getClientId())) {
            UserEntity newClient = userRepository.findByPublicId(request.getClientId())
                    .orElseThrow(() -> new UserNotFoundException("El nuevo cliente especificado no existe"));
            routineEntity.setClient(newClient);
        }


        if (!routineEntity.getProfessor().getPublicId().equals(request.getProfessorId())) {
            UserEntity newProfessor = userRepository.findByPublicId(request.getProfessorId())
                    .orElseThrow(() -> new UserNotFoundException("El nuevo profesor especificado no existe"));
            routineEntity.setProfessor(newProfessor);
        }


        routineMapperRequest.updateEntityFromDTO(request, routineEntity);

        return routineMapperResponse.convertToDto(routineRepository.save(routineEntity));
    }


    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    @Transactional
    public void deleteRoutine(UUID publicId) {
        RoutineEntity routine = routineRepository.findByPublicId(publicId)
                .orElseThrow(() -> new RoutineNotFoundException("No se encontró la rutina a eliminar"));

        routineRepository.delete(routine);
    }


}
