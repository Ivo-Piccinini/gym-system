package com.utnGymGroup.gym_system.features.subscription;
import com.utnGymGroup.gym_system.common.auth.credentials.CredentialsEntity;
import com.utnGymGroup.gym_system.common.auth.credentials.CredentialsRepository;
import com.utnGymGroup.gym_system.features.memberships.MembershipsEntity;
import com.utnGymGroup.gym_system.features.memberships.MembershipsRepository;
import com.utnGymGroup.gym_system.features.memberships.exceptions.MembershipNotFoundException;
import com.utnGymGroup.gym_system.features.subscription.exceptions.SubscriptionNotFoundException;
import com.utnGymGroup.gym_system.features.user.UserEntity;
import com.utnGymGroup.gym_system.features.user.exceptions.UserNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;


@Service
@RequiredArgsConstructor
public class SubscriptionsService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionsMapper subscriptionsMapper;
    private final MembershipsRepository membershipsRepository;
    private final CredentialsRepository credentialsRepository;

    public List<SubscriptionsDTO> getAllSubscriptions() {
        return subscriptionRepository.findAll()
                .stream()
                .map(subscriptionsMapper::convertToDto)
                .toList();
    }


    public List<SubscriptionsDTO> getMySubscriptions() {
        UserEntity user = getAuthenticatedUser();
        return subscriptionRepository.findByUser(user)
                .stream()
                .map(subscriptionsMapper::convertToDto)
                .toList();
    }


    @Transactional
    public SubscriptionsDTO subscribe(Long planId) {
        UserEntity user = getAuthenticatedUser();

        MembershipsEntity plan = membershipsRepository.findById(planId)
                .orElseThrow(() -> new MembershipNotFoundException("No se encontró el plan con ID: " + planId));

        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(plan.getDurationDays());

        SubscriptionsEntity subscription = new SubscriptionsEntity();
        subscription.setUser(user);
        subscription.setPlan(plan);
        subscription.setStartDate(startDate);
        subscription.setEndDate(endDate);
        subscription.setStatus(SubscriptionsStatus.ACTIVE);

        return subscriptionsMapper.convertToDto(subscriptionRepository.save(subscription));
    }


    @Transactional
    public SubscriptionsDTO cancelSubscription(Long id) {
        UserEntity authenticatedUser = getAuthenticatedUser();

        SubscriptionsEntity subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new SubscriptionNotFoundException("No se encontró la suscripción con ID: " + id));

        if (!subscription.getUser().getId().equals(authenticatedUser.getId())) {
            throw new SubscriptionNotFoundException("No tenés permiso para cancelar esta suscripción.");
        }

        subscription.setStatus(SubscriptionsStatus.CANCELED);
        return subscriptionsMapper.convertToDto(subscriptionRepository.save(subscription));
    }


    private UserEntity getAuthenticatedUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return credentialsRepository.findByUsername(username)
                .map(CredentialsEntity::getUser)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado. USERNAME: " + username));
    }

}
