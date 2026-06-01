package com.utnGymGroup.gym_system.features.exercise;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ExerciseRepository extends JpaRepository<ExerciseEntity, Long> {
    
    Optional<ExerciseEntity> findByIdPublic(String idPublic);
    Optional<ExerciseEntity> findByName(String name);
}