package com.utnGymGroup.gym_system.features.enrollment;

import com.utnGymGroup.gym_system.features.GymClass.GymClassEntity;
import com.utnGymGroup.gym_system.features.GymClass.GymClassRepository;
import com.utnGymGroup.gym_system.features.audit.AuditActions;
import com.utnGymGroup.gym_system.features.audit.Auditable;
import com.utnGymGroup.gym_system.features.user.UserEntity;
import com.utnGymGroup.gym_system.features.user.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final EnrollmentMapper enrollmentMapper;
    private final GymClassRepository gymClassRepository;
    private final UserService userService;

    @Transactional(readOnly = true)
    public List<EnrollmentDTO> getAllEnrollments() {
        return enrollmentRepository.findAll().stream()
                .map(enrollmentMapper::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EnrollmentDTO> getMyEnrollments() {

        UserEntity actualUser = userService.getAuthenticatedUserEntity();

        return enrollmentRepository.findByClient(actualUser).stream()
                .map(enrollmentMapper::convertToDto)
                .collect(Collectors.toList());
    }

    @Auditable(AuditActions.ENROLL_IN_CLASS)
    @Transactional
    public EnrollmentDTO enrollClient(UUID classExternalId) {

        UserEntity client = userService.getAuthenticatedUserEntity();

        GymClassEntity gymClass = gymClassRepository.findByExternalIdAndActiveTrue(classExternalId)
                .orElseThrow(() -> new EntityNotFoundException("Clase no encontrada con el ID: " + classExternalId));

        LocalDate enrollDate= LocalDate.now();
        long currentEnrolled = enrollmentRepository.countByGymClassIdAndEnrollmentDate(gymClass.getId(),enrollDate);
        if (currentEnrolled >= gymClass.getCapacityMax()) {
            throw new IllegalStateException("No se puede inscribir: La clase ha alcanzado su capacidad máxima de " + gymClass.getCapacityMax() + " alumnos.");
        }

        EnrollmentEntity enrollment = new EnrollmentEntity();
        enrollment.setClient(client);
        enrollment.setGymClass(gymClass);
        enrollment.setEnrollmentDate(LocalDate.now());

        EnrollmentEntity savedEnrollment = enrollmentRepository.save(enrollment);
        return enrollmentMapper.convertToDto(savedEnrollment);
    }

    @Auditable(AuditActions.CANCEL_ENROLLMENT)
    @Transactional
    public void cancelEnrollment(UUID externalId) {
        EnrollmentEntity enrollment = enrollmentRepository.findByExternalId(externalId)
                .orElseThrow(() -> new EntityNotFoundException("Inscripción no encontrada con el ID: " + externalId));

        UserEntity currentUser = userService.getAuthenticatedUserEntity();
        if (!enrollment.getClient().getId().equals(currentUser.getId())) {
            throw new IllegalStateException("No tienes permiso para cancelar esta inscripción porque no te pertenece.");
        }

        enrollmentRepository.delete(enrollment);
    }
}