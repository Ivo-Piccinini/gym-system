package com.utnGymGroup.gym_system.features.activity;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final ActivityMapper activityMapper;

    public ActivityService(ActivityRepository activityRepository, ActivityMapper activityMapper) {
        this.activityRepository = activityRepository;
        this.activityMapper = activityMapper;
    }

    @Transactional(readOnly = true)
    public List<ActivityDTO> getAllActivities() {
        return activityRepository.findByActiveTrue().stream()
                .map(activityMapper::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ActivityDTO getActivityByExternalId(UUID externalId) {
        ActivityEntity activityEntity = activityRepository.findByExternalIdAndActiveTrue(externalId)
                .orElseThrow(() -> new EntityNotFoundException("Actividad no encontrada o dada de baja con el ID: " + externalId));

        return activityMapper.convertToDto(activityEntity);
    }

    @Transactional
    public ActivityDTO createActivity(ActivityDTO activityDTO) {
        ActivityEntity activityEntity = activityMapper.convertToEntity(activityDTO);
        ActivityEntity savedActivity = activityRepository.save(activityEntity);
        return activityMapper.convertToDto(savedActivity);
    }

    @Transactional
    public ActivityDTO updateActivity(UUID externalId, ActivityDTO activityDTO) {
        ActivityEntity existingActivity = activityRepository.findByExternalIdAndActiveTrue(externalId)
                .orElseThrow(() -> new EntityNotFoundException("Actividad no encontrada para actualizar con el ID: " + externalId));

        existingActivity.setName(activityDTO.getName());
        existingActivity.setDescription(activityDTO.getDescription());

        ActivityEntity updatedActivity = activityRepository.save(existingActivity);
        return activityMapper.convertToDto(updatedActivity);
    }

    @Transactional
    public void deleteActivity(UUID externalId) {
        ActivityEntity activityEntity = activityRepository.findByExternalIdAndActiveTrue(externalId)
                .orElseThrow(() -> new EntityNotFoundException("Actividad no encontrada para eliminar con el ID: " + externalId));

        activityEntity.setActive(false);
        activityRepository.save(activityEntity);
    }
}