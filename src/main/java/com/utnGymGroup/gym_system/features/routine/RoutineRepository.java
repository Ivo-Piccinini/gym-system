package com.utnGymGroup.gym_system.features.routine;

import org.hibernate.dialect.lock.OptimisticEntityLockException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoutineRepository extends JpaRepository<RoutineEntity,Long>
{
    Optional<RoutineEntity> findByPublicId(UUID publicId);

    boolean existsByName(String name);

}
