package com.utnGymGroup.gym_system.features.payment;

import com.utnGymGroup.gym_system.features.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {
    List<PaymentEntity> findBySubscriptionUser(UserEntity user);
    Optional<PaymentEntity> findByPublicId(UUID publicId);
    List<PaymentEntity> findByStatus(PaymentStatus status);
}
