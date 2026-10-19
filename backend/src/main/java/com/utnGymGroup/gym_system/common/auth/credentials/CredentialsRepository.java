package com.utnGymGroup.gym_system.common.auth.credentials;

import com.utnGymGroup.gym_system.common.auth.permissions.Roles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CredentialsRepository extends JpaRepository<CredentialsEntity, Long> {
    Optional<CredentialsEntity> findByUsername(String username);
    List<CredentialsEntity> findAllByEnabled(Boolean enabled);
    boolean existsByUsername(String username);
    @Query("SELECT c FROM CredentialsEntity c JOIN c.roles r WHERE r.role = :role")
    List<CredentialsEntity> findAllByRole(@Param("role") Roles role);
    Optional<CredentialsEntity> findByUser_PublicId(UUID userPublicId);
}
