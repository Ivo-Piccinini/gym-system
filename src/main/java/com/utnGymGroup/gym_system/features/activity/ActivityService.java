package com.utnGymGroup.gym_system.features.activity;

import com.utnGymGroup.gym_system.features.GymClass.GymClassRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final ActivityMapper activityMapper;
    private final GymClassRepository gymClassRepository;

    @Transactional(readOnly = true)
    public List<ActivityDTO> getAllActivities() {
        return activityRepository.findAll().stream()
                .map(activityMapper::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ActivityDTO getActivityByExternalId(UUID externalId) {
        ActivityEntity entity = activityRepository.findByExternalId(externalId)
                .orElseThrow(() -> new EntityNotFoundException("Actividad no encontrada con el ID: " + externalId));
        return activityMapper.convertToDto(entity);
    }

    @Transactional
    public ActivityDTO createActivity(ActivityDTO activityDTO) {
        ActivityEntity entity = activityMapper.convertToEntity(activityDTO);
        ActivityEntity savedEntity = activityRepository.save(entity);
        return activityMapper.convertToDto(savedEntity);
    }

    @Transactional
    public ActivityDTO updateActivity(UUID externalId, ActivityDTO activityDTO) {
        ActivityEntity existingEntity = activityRepository.findByExternalId(externalId)
                .orElseThrow(() -> new EntityNotFoundException("Actividad no encontrada para actualizar con el ID: " + externalId));

        activityMapper.updateEntityFromDTO(activityDTO, existingEntity);

        ActivityEntity updatedEntity = activityRepository.save(existingEntity);
        return activityMapper.convertToDto(updatedEntity);
    }

    @Transactional
    public void deleteActivity(UUID externalId) {
        ActivityEntity entity = activityRepository.findByExternalId(externalId)
                .orElseThrow(() -> new EntityNotFoundException("Actividad no encontrada para eliminar con el ID: " + externalId));

        boolean isUsedInClasses = !gymClassRepository.findByActivityExternalId(externalId).isEmpty();
        if (isUsedInClasses) {
            throw new IllegalStateException("No se puede eliminar la actividad porque existen clases asignadas a ella.");
        }

        activityRepository.delete(entity);
    }
}