package com.utnGymGroup.gym_system.features.subscription;

import com.utnGymGroup.gym_system.features.membership.MembershipEntity;
import com.utnGymGroup.gym_system.features.membership.MembershipRepository;
import com.utnGymGroup.gym_system.features.membership.exceptions.MembershipNotFoundException;
import com.utnGymGroup.gym_system.features.subscription.dtos.SubscriptionResponseDto;
import com.utnGymGroup.gym_system.features.subscription.exceptions.SubscriptionAlreadyActiveException;
import com.utnGymGroup.gym_system.features.subscription.exceptions.SubscriptionNotFoundException;
import com.utnGymGroup.gym_system.features.subscription.mappers.SubscriptionRequestMapper;
import com.utnGymGroup.gym_system.features.subscription.mappers.SubscriptionResponseMapper;
import com.utnGymGroup.gym_system.features.user.UserEntity;
import com.utnGymGroup.gym_system.features.user.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionRequestMapper requestMapper;
    private final SubscriptionResponseMapper responseMapper;
    private final MembershipRepository membershipRepository;
    private final UserService userService;

    public List<SubscriptionResponseDto> getAllSubscriptions() {
        return subscriptionRepository.findAll()
                .stream()
                .map(responseMapper::convertToDto)
                .toList();
    }

    public List<SubscriptionResponseDto> getMySubscriptions() {
        UserEntity user = userService.getAuthenticatedUserEntity();
        List<SubscriptionEntity> subscriptions = subscriptionRepository.findByUser(user);

        subscriptions.forEach(sub -> {
            if (sub.getStatus() == SubscriptionStatus.ACTIVE && sub.getEndDate().isBefore(LocalDate.now())) {
                sub.setStatus(SubscriptionStatus.EXPIRED);
                subscriptionRepository.save(sub);
            }
        });

        return subscriptions.stream()
                .map(responseMapper::convertToDto)
                .toList();
    }

    @Transactional
    public SubscriptionResponseDto subscribe(UUID planId) {
        UserEntity user = userService.getAuthenticatedUserEntity();

        MembershipEntity plan = membershipRepository.findByUUID(planId)
                .orElseThrow(() -> new MembershipNotFoundException("No se encontró el plan con ID: " + planId));

        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(plan.getDurationDays());

        SubscriptionEntity subscription = new SubscriptionEntity();
        subscription.setUser(user);
        subscription.setPlan(plan);
        subscription.setStartDate(startDate);
        subscription.setEndDate(endDate);
        subscription.setStatus(SubscriptionStatus.ACTIVE);

        return responseMapper.convertToDto(subscriptionRepository.save(subscription));
    }

    @Transactional
    public SubscriptionResponseDto cancelSubscription(UUID id) {
        UserEntity authenticatedUser = userService.getAuthenticatedUserEntity();

        SubscriptionEntity subscription = subscriptionRepository.findByUUID(id)
                .orElseThrow(() -> new SubscriptionNotFoundException("No se encontró la suscripción con ID: " + id));

        if (!subscription.getUser().getPublicId().equals(authenticatedUser.getPublicId())) {
            throw new SubscriptionNotFoundException("No tenés permiso para cancelar esta suscripción.");
        }

        if (subscription.getStatus() == SubscriptionStatus.CANCELED) {
            throw new SubscriptionAlreadyActiveException("La suscripción ya está cancelada.");
        }

        if (subscription.getStatus() == SubscriptionStatus.EXPIRED) {
            throw new SubscriptionAlreadyActiveException("No se puede cancelar una suscripción vencida.");
        }

        subscription.setStatus(SubscriptionStatus.CANCELED);
        return responseMapper.convertToDto(subscriptionRepository.save(subscription));
    }

}