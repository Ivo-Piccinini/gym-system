package com.utnGymGroup.gym_system.features.GymClass;

import com.utnGymGroup.gym_system.features.GymClass.exceptions.ScheduleOverlapException;
import com.utnGymGroup.gym_system.features.activity.ActivityEntity;
import com.utnGymGroup.gym_system.features.activity.ActivityRepository;
import com.utnGymGroup.gym_system.features.enrollment.EnrollmentRepository;
import com.utnGymGroup.gym_system.features.user.UserEntity;
import com.utnGymGroup.gym_system.features.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GymClassService {

    private final GymClassRepository gymClassRepository;
    private final GymClassMapper gymClassMapper;
    private final EnrollmentRepository enrollmentRepository;
    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;

    public GymClassService(GymClassRepository gymClassRepository,
                           GymClassMapper gymClassMapper,
                           EnrollmentRepository enrollmentRepository,
                           ActivityRepository activityRepository,
                           UserRepository userRepository) {
        this.gymClassRepository = gymClassRepository;
        this.gymClassMapper = gymClassMapper;
        this.enrollmentRepository = enrollmentRepository;
        this.activityRepository = activityRepository;
        this.userRepository = userRepository;
    }

    /** Atajo sin parámetros: lista las clases con sus cupos. */
    @Transactional(readOnly = true)
    public List<GymClassDTO> getAllClasses() {
        return getAllClasses(null);
    }

    /**
     * Lista las clases activas con sus cupos.
     * enrolledCount = inscripciones vigentes de la clase; availableSpots = capacityMax - enrolledCount
     * (mínimo 0). Las clases se repiten todas las semanas y la inscripción dura hasta que el socio
     * la cancela, así que el cupo no depende de una fecha. El parámetro se conserva por
     * compatibilidad con el endpoint y se ignora.
     */
    @Transactional(readOnly = true)
    public List<GymClassDTO> getAllClasses(LocalDate date) {
        return gymClassRepository.findByActiveTrue().stream()
                .map(entity -> {
                    GymClassDTO dto = gymClassMapper.convertToDto(entity);
                    int enrolled = (int) enrollmentRepository.countByGymClassId(entity.getId());
                    dto.setEnrolledCount(enrolled);
                    dto.setAvailableSpots(Math.max(0, entity.getCapacityMax() - enrolled));
                    return dto;
                })
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

        // Se buscan la disciplina y el profesor que ya existen. No se usa el mapper para
        // armarlos: generaba entidades nuevas sin id y Hibernate lanzaba TransientPropertyValueException.
        ActivityEntity activity = activityRepository
                .findByExternalIdAndActiveTrue(gymClassDTO.getActivity().getExternalId())
                .orElseThrow(() -> new EntityNotFoundException("Disciplina no encontrada o dada de baja."));

        UserEntity professor = userRepository
                .findByPublicId(gymClassDTO.getProfessor().getPublicId())
                .orElseThrow(() -> new EntityNotFoundException("Profesor no encontrado."));

        boolean hasOverlap = gymClassRepository.existsOverlap(
                professor.getPublicId(),
                gymClassDTO.getDayOfWeek(),
                gymClassDTO.getStartTime(),
                gymClassDTO.getEndTime()
        );

        if (hasOverlap) {
            throw new ScheduleOverlapException("El profesor ya tiene una clase asignada en ese día y rango horario.");
        }

        GymClassEntity gymClassEntity = new GymClassEntity();
        gymClassEntity.setActivity(activity);
        gymClassEntity.setProfessor(professor);
        gymClassEntity.setDayOfWeek(gymClassDTO.getDayOfWeek());
        gymClassEntity.setStartTime(gymClassDTO.getStartTime());
        gymClassEntity.setEndTime(gymClassDTO.getEndTime());
        gymClassEntity.setCapacityMax(gymClassDTO.getCapacityMax());

        GymClassEntity savedClass = gymClassRepository.save(gymClassEntity);
        return gymClassMapper.convertToDto(savedClass);
    }

    @Transactional
    public GymClassDTO updateClass(UUID externalId, GymClassDTO gymClassDTO) {
        GymClassEntity existingClass = gymClassRepository.findByExternalIdAndActiveTrue(externalId)
                .orElseThrow(() -> new EntityNotFoundException("Clase no encontrada para actualizar con el ID: " + externalId));

        validateClassHours(gymClassDTO);

        // La disciplina y el profesor también se pueden cambiar. Si el pedido no los trae,
        // se mantienen los actuales.
        ActivityEntity activity = existingClass.getActivity();
        if (gymClassDTO.getActivity() != null && gymClassDTO.getActivity().getExternalId() != null) {
            activity = activityRepository
                    .findByExternalIdAndActiveTrue(gymClassDTO.getActivity().getExternalId())
                    .orElseThrow(() -> new EntityNotFoundException("Disciplina no encontrada o dada de baja."));
        }

        UserEntity professor = existingClass.getProfessor();
        if (gymClassDTO.getProfessor() != null && gymClassDTO.getProfessor().getPublicId() != null) {
            professor = userRepository
                    .findByPublicId(gymClassDTO.getProfessor().getPublicId())
                    .orElseThrow(() -> new EntityNotFoundException("Profesor no encontrado."));
        }

        // La superposición se valida con el profesor resultante (el nuevo, si se cambió).
        boolean hasOverlap = gymClassRepository.existsOverlapForUpdate(
                professor.getPublicId(),
                gymClassDTO.getDayOfWeek(),
                gymClassDTO.getStartTime(),
                gymClassDTO.getEndTime(),
                externalId
        );

        if (hasOverlap) {
            throw new ScheduleOverlapException("No se puede actualizar: El profesor ya tiene otra clase asignada en ese horario.");
        }

        existingClass.setActivity(activity);
        existingClass.setProfessor(professor);
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
        return gymClassRepository.findByProfessorPublicIdAndActiveTrue(professorExternalId).stream()
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
