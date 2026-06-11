package com.utnGymGroup.gym_system.features.payments;

import com.utnGymGroup.gym_system.common.auth.credentials.CredentialsEntity;
import com.utnGymGroup.gym_system.common.auth.credentials.CredentialsRepository;
import com.utnGymGroup.gym_system.features.payments.dtos.PaymentsRequestDTO;
import com.utnGymGroup.gym_system.features.payments.dtos.PaymentsResponseDto; // 🎯 Importación del DTO de respuesta
import com.utnGymGroup.gym_system.features.payments.mappers.PaymentRequestMapper; // 🎯 Importación del mapper de entrada
import com.utnGymGroup.gym_system.features.payments.mappers.PaymentsResponseMapper; // 🎯 Importación del mapper de salida
import com.utnGymGroup.gym_system.features.subscription.SubscriptionRepository;
import com.utnGymGroup.gym_system.features.subscription.SubscriptionsEntity;
import com.utnGymGroup.gym_system.features.subscription.exceptions.SubscriptionNotFoundException;
import com.utnGymGroup.gym_system.features.user.UserEntity;
import com.utnGymGroup.gym_system.features.user.exceptions.UserNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentsService {

    private final PaymentsRepository paymentsRepository;
    private final PaymentRequestMapper requestMapper;
    private final PaymentsResponseMapper responseMapper;
    private final SubscriptionRepository subscriptionRepository;
    private final CredentialsRepository credentialsRepository;


    @Transactional
    public PaymentsResponseDto createPayment(PaymentsRequestDTO dto) {
        SubscriptionsEntity subscription = subscriptionRepository.findById(dto.getSubscriptionId())
                .orElseThrow(() -> new SubscriptionNotFoundException("No se encontró la suscripción con ID: " + dto.getSubscriptionId()));

        PaymentsEntity payment = new PaymentsEntity();
        payment.setSubscription(subscription);
        payment.setAmount(dto.getAmount());
        payment.setPaymentDate(dto.getPaymentDate());
        payment.setMethod(dto.getMethod());

        return responseMapper.convertToDto(paymentsRepository.save(payment));
    }


    public List<PaymentsResponseDto> getMyPayments() {
        UserEntity user = getAuthenticatedUser();
        return paymentsRepository.findBySubscriptionUser(user)
                .stream()
                .map(responseMapper::convertToDto)
                .toList();
    }


    public List<PaymentsResponseDto> getAllPayments() {
        return paymentsRepository.findAll()
                .stream()
                .map(responseMapper::convertToDto)
                .toList();
    }

    private UserEntity getAuthenticatedUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return credentialsRepository.findByUsername(username)
                .map(CredentialsEntity::getUser)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado. USERNAME: " + username));
    }
}