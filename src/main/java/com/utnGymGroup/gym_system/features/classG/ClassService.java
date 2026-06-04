package com.utnGymGroup.gym_system.features.classG;


import com.utnGymGroup.gym_system.features.enrollment.EnrollmentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ClassService {

    private final ClassRepository classRepository;
    private final ClassMapper classMapper;
    private final EnrollmentRepository enrollmentRepository;

    public ClassService(ClassRepository classRepository, ClassMapper classMapper, EnrollmentRepository enrollmentRepository) {
        this.classRepository = classRepository;
        this.classMapper = classMapper;
        this.enrollmentRepository = enrollmentRepository;
    }


    @Transactional(readOnly = true)
    public List<ClassDTO> getAllClasses() {
        return classRepository.findAll().stream()
                .map(classMapper::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ClassDTO getClassByExternalId(UUID externalId) {
        ClassEntity classEntity = classRepository.findByExternalId(externalId)
                .orElseThrow(() -> new EntityNotFoundException("Clase no encontrada con el ID: " + externalId));

        return classMapper.convertToDto(classEntity);
    }

    @Transactional
    public ClassDTO createClass(ClassDTO classDTO) {
        validateClassHours(classDTO);

        // CORRECCIÓN: El username ya es un String, no necesitas UUID.fromString()
        // Asegúrate de que classDTO.getProfessor() no sea null antes de llamar a getUsername()
        String professorUsername = classDTO.getProfessor().getUsername();

        boolean hasOverlap = classRepository.existsOverlap(
                professorUsername,
                classDTO.getDayOfWeek(),
                classDTO.getStartTime(),
                classDTO.getEndTime()
        );

        if (hasOverlap) {
            throw new IllegalArgumentException("El profesor ya tiene una clase asignada en ese día y rango horario.");
        }

        ClassEntity classEntity = classMapper.convertToEntity(classDTO);
        
        ClassEntity savedClass = classRepository.save(classEntity);
        return classMapper.convertToDto(savedClass);
    }

    @Transactional
    public ClassDTO updateClass(UUID externalId, ClassDTO classDTO) {
        ClassEntity existingClass = classRepository.findByExternalId(externalId)
                .orElseThrow(() -> new EntityNotFoundException("Clase no encontrada para actualizar con el ID: " + externalId));

        validateClassHours(classDTO);

        boolean hasOverlap = classRepository.existsOverlapForUpdate(
                classDTO.getProfessor().getUsername(),
                classDTO.getDayOfWeek(),
                classDTO.getStartTime(),
                classDTO.getEndTime(),
                externalId
        );

        if (hasOverlap) {
            throw new IllegalArgumentException("No se puede actualizar: El profesor ya tiene otra clase asignada en ese horario.");
        }

        existingClass.setDayOfWeek(classDTO.getDayOfWeek());
        existingClass.setStartTime(classDTO.getStartTime());
        existingClass.setEndTime(classDTO.getEndTime());
        existingClass.setCapacityMax(classDTO.getCapacityMax());

        ClassEntity updatedClass = classRepository.save(existingClass);
        return classMapper.convertToDto(updatedClass);
    }

    @Transactional
    public void deleteClass(UUID externalId) {
        ClassEntity classEntity = classRepository.findByExternalId(externalId)
                .orElseThrow(() -> new EntityNotFoundException("Clase no encontrada para eliminar con el ID: " + externalId));

        long totalEnrolled = enrollmentRepository.countByGymClassId(classEntity.getId());
        if (totalEnrolled > 0) {
            throw new IllegalStateException("No se puede eliminar la clase porque ya tiene " + totalEnrolled + " alumno(s) inscripto(s).");
        }

        classRepository.delete(classEntity);
    }

    @Transactional(readOnly = true)
    public List<ClassDTO> getClassesByProfessor(UUID professorExternalId) {
        // Se usa findByProfessorExternalId tal como lo definiste en tu repositorio
        return classRepository.findByProfessorExternalId(professorExternalId).stream()
                .map(classMapper::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ClassDTO> getClassesByActivity(UUID activityExternalId) {
        // Se usa findByActivityExternalId tal como lo definiste en tu repositorio
        return classRepository.findByActivityExternalId(activityExternalId).stream()
                .map(classMapper::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ClassDTO> getClassesByDay(DayOfWeek dayOfWeek) {
        return classRepository.findAllByDayOfWeek(dayOfWeek).stream()
                .map(classMapper::convertToDto)
                .collect(Collectors.toList());
    }


    private void validateClassHours(ClassDTO classDTO) {
        if (classDTO.getStartTime() == null || classDTO.getEndTime() == null) {
            throw new IllegalArgumentException("Los horarios de inicio y fin son obligatorios.");
        }
        if (classDTO.getStartTime().isAfter(classDTO.getEndTime()) || classDTO.getStartTime().equals(classDTO.getEndTime())) {
            throw new IllegalArgumentException("La hora de inicio debe ser estrictamente anterior a la hora de finalización.");
        }
    }
}