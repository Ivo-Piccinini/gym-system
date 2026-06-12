package com.utnGymGroup.gym_system.features.fullRoutine;

import com.utnGymGroup.gym_system.features.exercise.ExerciseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FullRoutineRepository extends JpaRepository<FullRoutineEntity,Long>
{
    Optional<ExerciseEntity> findBy(String nombre);

}
