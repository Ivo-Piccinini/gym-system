package com.utnGymGroup.gym_system.features.membership;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MembershipRepository extends JpaRepository<MembershipEntity, Long> {
    Boolean existsByName(String name);
    Optional<MembershipEntity> findByUUID(UUID id);
}
