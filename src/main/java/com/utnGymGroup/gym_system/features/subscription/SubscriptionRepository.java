package com.utnGymGroup.gym_system.features.subscription;

import com.utnGymGroup.gym_system.features.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubscriptionRepository extends JpaRepository<SubscriptionEntity, Long> {


    List<SubscriptionEntity> findByUser(UserEntity user);
}
