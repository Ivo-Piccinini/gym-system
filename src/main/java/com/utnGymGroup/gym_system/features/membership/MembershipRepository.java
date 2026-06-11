package com.utnGymGroup.gym_system.features.membership;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MembershipRepository extends JpaRepository<MembershipEntity, Long> {
    Boolean existsByName(String name);
}
