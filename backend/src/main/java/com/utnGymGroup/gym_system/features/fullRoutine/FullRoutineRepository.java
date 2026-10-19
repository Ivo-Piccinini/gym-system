package com.utnGymGroup.gym_system.features.fullRoutine;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FullRoutineRepository extends JpaRepository<FullRoutineEntity, Long>
{
    Optional<FullRoutineEntity> findByPublicId(UUID publicId);

    @org.springframework.data.jpa.repository.Query("SELECT f FROM FullRoutineEntity f WHERE f.routine.publicId = :routinePublicId AND f.enabled = true")
    java.util.List<FullRoutineEntity> findAllByRoutinePublicId(@org.springframework.data.repository.query.Param("routinePublicId") UUID routinePublicId);

}
