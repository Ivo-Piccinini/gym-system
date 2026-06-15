package com.utnGymGroup.gym_system.features.GymClass;

import com.utnGymGroup.gym_system.features.enrollment.EnrollmentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GymClassService {

    private final GymClassRepository gymClassRepository;
    private final GymClassMapper gymClassMapper;
    private final EnrollmentRepository enrollmentRepository;

    public GymClassService(GymClassRepository gymClassRepository, GymClassMapper gymClassMapper, EnrollmentRepository enrollmentRepository) {
        this.gymClassRepository = gymClassRepository;
        this.gymClassMapper = gymClassMapper;
        this.enrollmentRepository = enrollmentRepository;
    }

    @Transactional(readOnly = true)
    public List<GymClassDTO> getAllClasses() {
        return gymClassRepository.findByActiveTrue().stream()
                .map(gymClassMapper::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public GymClassDTO getClassByExternalId(UUID externalId) {
        GymClassEntity gymClassEntity = gymClassRepository.findByExternalIdAndActiveTrue(externalId)
                .orElseThrow(() -> new EntityNotFoundException("Clase no encontrada o dada de baja con el ID: " + externalId));

        return gymClassMapper.convertToDto(gymClassEntity);
    }

    @Transactional
    public GymClassDTO createClass(GymClassDTO gymClassDTO) {
        validateClassHours(gymClassDTO);

        String professorFirstName = gymClassDTO.getProfessor().getFirstName();

        boolean hasOverlap = gymClassRepository.existsOverlap(
                professorFirstName,
                gymClassDTO.getDayOfWeek(),
                gymClassDTO.getStartTime(),
                gymClassDTO.getEndTime()
        );

        if (hasOverlap) {
            throw new IllegalArgumentException("El profesor ya tiene una clase asignada en ese día y rango horario.");
        }

        GymClassEntity gymClassEntity = gymClassMapper.convertToEntity(gymClassDTO);
        GymClassEntity savedClass = gymClassRepository.save(gymClassEntity);
        return gymClassMapper.convertToDto(savedClass);
    }

    @Transactional
    public GymClassDTO updateClass(UUID externalId, GymClassDTO gymClassDTO) {
        GymClassEntity existingClass = gymClassRepository.findByExternalIdAndActiveTrue(externalId)
                .orElseThrow(() -> new EntityNotFoundException("Clase no encontrada para actualizar con el ID: " + externalId));

        validateClassHours(gymClassDTO);

        String professorFirstName = gymClassDTO.getProfessor().getFirstName();

        boolean hasOverlap = gymClassRepository.existsOverlapForUpdate(
                professorFirstName,
                gymClassDTO.getDayOfWeek(),
                gymClassDTO.getStartTime(),
                gymClassDTO.getEndTime(),
                externalId
        );

        if (hasOverlap) {
            throw new IllegalArgumentException("No se puede actualizar: El profesor ya tiene otra clase asignada en ese horario.");
        }

        existingClass.setDayOfWeek(gymClassDTO.getDayOfWeek());
        existingClass.setStartTime(gymClassDTO.getStartTime());
        existingClass.setEndTime(gymClassDTO.getEndTime());
        existingClass.setCapacityMax(gymClassDTO.getCapacityMax());

        GymClassEntity updatedClass = gymClassRepository.save(existingClass);
        return gymClassMapper.convertToDto(updatedClass);
    }

    @Transactional
    public void deleteClass(UUID externalId) {
        GymClassEntity gymClassEntity = gymClassRepository.findByExternalIdAndActiveTrue(externalId)
                .orElseThrow(() -> new EntityNotFoundException("Clase no encontrada para eliminar con el ID: " + externalId));

        long totalEnrolled = enrollmentRepository.countByGymClassId(gymClassEntity.getId());
        if (totalEnrolled > 0) {
            throw new IllegalStateException("No se puede eliminar la clase porque ya tiene " + totalEnrolled + " alumno(s) inscripto(s).");
        }

        gymClassEntity.setActive(false);
        gymClassRepository.save(gymClassEntity);
    }

    @Transactional(readOnly = true)
    public List<GymClassDTO> getClassesByProfessor(UUID professorExternalId) {
        return gymClassRepository.findByProfessorExternalIdAndActiveTrue(professorExternalId).stream()
                .map(gymClassMapper::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<GymClassDTO> getClassesByActivity(UUID activityExternalId) {
        return gymClassRepository.findByActivityExternalIdAndActiveTrue(activityExternalId).stream()
                .map(gymClassMapper::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<GymClassDTO> getClassesByDay(DayOfWeek dayOfWeek) {
        return gymClassRepository.findAllByDayOfWeekAndActiveTrue(dayOfWeek).stream()
                .map(gymClassMapper::convertToDto)
                .collect(Collectors.toList());
    }

    private void validateClassHours(GymClassDTO gymClassDTO) {
        if (gymClassDTO.getStartTime() == null || gymClassDTO.getEndTime() == null) {
            throw new IllegalArgumentException("Los horarios de inicio y fin son obligatorios.");
        }
        if (gymClassDTO.getStartTime().isAfter(gymClassDTO.getEndTime()) || gymClassDTO.getStartTime().equals(gymClassDTO.getEndTime())) {
            throw new IllegalArgumentException("La hora de inicio debe ser estrictamente anterior a la hora de finalización.");
        }
    }
}