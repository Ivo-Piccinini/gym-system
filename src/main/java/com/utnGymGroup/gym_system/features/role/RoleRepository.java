package com.utnGymGroup.gym_system.features.role;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<RoleEntity, Long> {
    Optional<RoleEntity> findByPublicId(UUID externalId);
    Optional<RoleEntity> findByName(Roles name);
}
