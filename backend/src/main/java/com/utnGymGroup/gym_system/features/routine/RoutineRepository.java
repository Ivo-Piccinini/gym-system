package com.utnGymGroup.gym_system.features.routine;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoutineRepository extends JpaRepository<RoutineEntity,Long>
{
    Optional<RoutineEntity> findByPublicId(UUID publicId);

    Optional<RoutineEntity> findByPublicIdAndEnabledTrue(UUID publicId);

    List<RoutineEntity> findAllByEnabledTrueOrderByStartDateDesc();

    // Un cliente puede tener rutinas dadas de baja; la vigente es la última habilitada.
    Optional<RoutineEntity> findFirstByClient_PublicIdAndEnabledTrueOrderByIdDesc(UUID clientPublicId);

    boolean existsByName(String name);

}