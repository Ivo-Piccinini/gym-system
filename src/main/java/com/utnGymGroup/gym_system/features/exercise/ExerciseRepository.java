package com.utnGymGroup.gym_system.features.exercise;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExerciseRepository extends JpaRepository<ExerciseEntity, Long> {
    
    Optional<ExerciseEntity> findByIdPublic(UUID idPublic);
    Optional<ExerciseEntity> findByName(String name);
    boolean existsByName(String ExerciseName);


    List<ExerciseEntity> findByMuscleGroupAndEnabledTrue(String muscleGroup);
}