package com.utnGymGroup.gym_system.features.subscription;

import com.utnGymGroup.gym_system.common.auth.credentials.CredentialsEntity;
import com.utnGymGroup.gym_system.common.auth.credentials.CredentialsRepository;
import com.utnGymGroup.gym_system.features.memberships.MembershipsEntity;
import com.utnGymGroup.gym_system.features.memberships.MembershipsRepository;
import com.utnGymGroup.gym_system.features.memberships.exceptions.MembershipNotFoundException;
import com.utnGymGroup.gym_system.features.subscription.dtos.SubscriptionsRequestDTO;
import com.utnGymGroup.gym_system.features.subscription.dtos.SubscriptionsResponseDto;
import com.utnGymGroup.gym_system.features.subscription.exceptions.SubscriptionAlreadyActiveException;
import com.utnGymGroup.gym_system.features.subscription.exceptions.SubscriptionNotFoundException;
import com.utnGymGroup.gym_system.features.subscription.mappers.SubscriptionsRequestMapper;
import com.utnGymGroup.gym_system.features.subscription.mappers.SubscriptionsResponseMapper;
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
    private final SubscriptionsRequestMapper requestMapper;
    private final SubscriptionsResponseMapper responseMapper;
    private final MembershipsRepository membershipsRepository;
    private final CredentialsRepository credentialsRepository;

    public List<SubscriptionsResponseDto> getAllSubscriptions() {
        return subscriptionRepository.findAll()
                .stream()
                .map(responseMapper::convertToDto)
                .toList();
    }

    public List<SubscriptionsResponseDto> getMySubscriptions() {
        UserEntity user = getAuthenticatedUser();
        List<SubscriptionsEntity> subscriptions = subscriptionRepository.findByUser(user);

        subscriptions.forEach(sub -> {
            if (sub.getStatus() == SubscriptionsStatus.ACTIVE && sub.getEndDate().isBefore(LocalDate.now())) {
                sub.setStatus(SubscriptionsStatus.EXPIRED);
                subscriptionRepository.save(sub);
            }
        });

        return subscriptions.stream()
                .map(responseMapper::convertToDto)
                .toList();
    }

    @Transactional
    public SubscriptionsResponseDto subscribe(Long planId) {
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

        return responseMapper.convertToDto(subscriptionRepository.save(subscription));
    }

    @Transactional
    public SubscriptionsResponseDto cancelSubscription(Long id) {
        UserEntity authenticatedUser = getAuthenticatedUser();

        SubscriptionsEntity subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new SubscriptionNotFoundException("No se encontró la suscripción con ID: " + id));

        if (!subscription.getUser().getId().equals(authenticatedUser.getId())) {
            throw new SubscriptionNotFoundException("No tenés permiso para cancelar esta suscripción.");
        }

        if (subscription.getStatus() == SubscriptionsStatus.CANCELED) {
            throw new SubscriptionAlreadyActiveException("La suscripción ya está cancelada.");
        }

        if (subscription.getStatus() == SubscriptionsStatus.EXPIRED) {
            throw new SubscriptionAlreadyActiveException("No se puede cancelar una suscripción vencida.");
        }

        subscription.setStatus(SubscriptionsStatus.CANCELED);
        return responseMapper.convertToDto(subscriptionRepository.save(subscription));
    }

    private UserEntity getAuthenticatedUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return credentialsRepository.findByUsername(username)
                .map(CredentialsEntity::getUser)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado. USERNAME: " + username));
    }
}