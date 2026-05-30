package com.utnGymGroup.gym_system.common.auth.credentials;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CredentialsRepository extends JpaRepository<CredentialsEntity, Long> {
    Optional<CredentialsEntity> findByUsername(String username);
    List<CredentialsEntity> findAllByEnabled(Boolean enabled);
    boolean existsByUsername(String username);
}
