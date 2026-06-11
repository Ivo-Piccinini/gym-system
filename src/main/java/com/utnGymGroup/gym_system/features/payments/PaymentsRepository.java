package com.utnGymGroup.gym_system.features.payments;

import com.utnGymGroup.gym_system.features.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Arrays;
import java.util.List;

public interface PaymentsRepository extends JpaRepository<PaymentsEntity, Long> {
    List<PaymentsEntity> findBySubscriptionUser(UserEntity user);
}
