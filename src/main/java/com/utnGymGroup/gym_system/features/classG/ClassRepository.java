package com.utnGymGroup.gym_system.features.classG;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClassRepository extends JpaRepository<ClassEntity, Long> {
    Optional<ClassEntity> findByExternalId(UUID externalId);

    List<ClassEntity> findByDayOfWeek(DayOfWeek day);
}
