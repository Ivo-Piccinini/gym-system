package com.utnGymGroup.gym_system.features.activity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ActivityRepository extends JpaRepository<ActivityEntity, Long> {


    Optional<ActivityEntity> findByExternalIdAndActiveTrue(UUID externalId);

    List<ActivityEntity> findByActiveTrue();

}