package com.utnGymGroup.gym_system.features.access;

import com.utnGymGroup.gym_system.features.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccessPassRepository extends JpaRepository<AccessPassEntity, Long> {
    Optional<AccessPassEntity> findByPublicId(UUID publicId);
    Optional<AccessPassEntity> findByAccessCode(String accessCode);
    Optional<AccessPassEntity> findFirstByUserAndActiveTrueOrderByGeneratedAtDesc(UserEntity user);
    List<AccessPassEntity> findByUser(UserEntity user);
}
