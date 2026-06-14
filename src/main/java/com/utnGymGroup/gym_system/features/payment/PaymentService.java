package com.utnGymGroup.gym_system.features.payment;

import com.utnGymGroup.gym_system.features.audit.AuditActions;
import com.utnGymGroup.gym_system.features.audit.Auditable;
import com.utnGymGroup.gym_system.features.payment.dtos.PaymentRequestDTO;
import com.utnGymGroup.gym_system.features.payment.dtos.PaymentResponseDto; // 🎯 Importación del DTO de respuesta
import com.utnGymGroup.gym_system.features.payment.mappers.PaymentRequestMapper; // 🎯 Importación del mapper de entrada
import com.utnGymGroup.gym_system.features.payment.mappers.PaymentResponseMapper; // 🎯 Importación del mapper de salida
import com.utnGymGroup.gym_system.features.subscription.SubscriptionRepository;
import com.utnGymGroup.gym_system.features.subscription.SubscriptionEntity;
import com.utnGymGroup.gym_system.features.subscription.exceptions.SubscriptionNotFoundException;
import com.utnGymGroup.gym_system.features.user.UserEntity;
import com.utnGymGroup.gym_system.features.user.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentRequestMapper requestMapper;
    private final PaymentResponseMapper responseMapper;
    private final SubscriptionRepository subscriptionRepository;
    private final UserService userService;


    @Auditable(AuditActions.PROCESS_PAYMENT)
    @Transactional
    public PaymentResponseDto createPayment(PaymentRequestDTO dto) {
        SubscriptionEntity subscription = subscriptionRepository.findById(dto.getSubscriptionId())
                .orElseThrow(() -> new SubscriptionNotFoundException("No se encontró la suscripción con ID: " + dto.getSubscriptionId()));

        PaymentEntity payment = new PaymentEntity();
        payment.setSubscription(subscription);
        payment.setAmount(dto.getAmount());
        payment.setPaymentDate(dto.getPaymentDate());
        payment.setMethod(dto.getMethod());

        return responseMapper.convertToDto(paymentRepository.save(payment));
    }


    public List<PaymentResponseDto> getMyPayments() {
        UserEntity user = userService.getAuthenticatedUserEntity();
        return paymentRepository.findBySubscriptionUser(user)
                .stream()
                .map(responseMapper::convertToDto)
                .toList();
    }


    public List<PaymentResponseDto> getAllPayments() {
        return paymentRepository.findAll()
                .stream()
                .map(responseMapper::convertToDto)
                .toList();
    }

}