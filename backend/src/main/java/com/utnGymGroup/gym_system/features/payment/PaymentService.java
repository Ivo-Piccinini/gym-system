package com.utnGymGroup.gym_system.features.payment;

import com.utnGymGroup.gym_system.features.audit.AuditActions;
import com.utnGymGroup.gym_system.features.audit.Auditable;
import com.utnGymGroup.gym_system.features.payment.dtos.PaymentRequestDTO;
import com.utnGymGroup.gym_system.features.payment.dtos.PaymentResponseDto;
import com.utnGymGroup.gym_system.features.payment.mappers.PaymentRequestMapper;
import com.utnGymGroup.gym_system.features.payment.mappers.PaymentResponseMapper;
import com.utnGymGroup.gym_system.features.subscription.SubscriptionRepository;
import com.utnGymGroup.gym_system.features.subscription.SubscriptionEntity;
import com.utnGymGroup.gym_system.features.subscription.SubscriptionStatus;
import com.utnGymGroup.gym_system.features.subscription.exceptions.SubscriptionNotFoundException;
import com.utnGymGroup.gym_system.features.user.UserEntity;
import com.utnGymGroup.gym_system.features.user.UserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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
        assertNotAdminUser();
        SubscriptionEntity subscription = subscriptionRepository.findByPublicId(dto.getSubscriptionId())
                .orElseThrow(() -> new SubscriptionNotFoundException("No se encontró la suscripción con ID: " + dto.getSubscriptionId()));

        PaymentEntity payment = new PaymentEntity();
        payment.setSubscription(subscription);
        payment.setAmount(dto.getAmount());
        payment.setPaymentDate(dto.getPaymentDate());
        payment.setMethod(dto.getMethod());

        if (dto.getMethod() == PaymentMethods.TARJETA) {
            payment.setStatus(PaymentStatus.APPROVED);
            payment.setValidatedBy("SISTEMA");
            payment.setValidationDate(LocalDateTime.now());
            subscription.setStatus(SubscriptionStatus.ACTIVE);
            subscription.setStartDate(LocalDate.now());
            if (subscription.getPlan() != null) {
                subscription.setEndDate(LocalDate.now().plusDays(subscription.getPlan().getDurationDays()));
            }
        } else {
            // Pagos en efectivo: quedan pendientes hasta que el admin valide en recepción
            payment.setStatus(PaymentStatus.PENDING);
            payment.setValidatedBy(null);
            payment.setValidationDate(null);
            subscription.setStatus(SubscriptionStatus.PENDING);
        }

        subscriptionRepository.save(subscription);
        return responseMapper.convertToDto(paymentRepository.save(payment));
    }

    @Auditable(AuditActions.PROCESS_PAYMENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public PaymentResponseDto validateCashPayment(UUID paymentPublicId) {
        PaymentEntity payment = paymentRepository.findByPublicId(paymentPublicId)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró el pago con ID: " + paymentPublicId));

        if (payment.getMethod() != PaymentMethods.EFECTIVO) {
            throw new IllegalStateException("Solo se pueden validar manualmente pagos registrados en efectivo.");
        }
        if (payment.getStatus() == PaymentStatus.APPROVED) {
            throw new IllegalStateException("Este pago ya fue validado y aprobado previamente.");
        }

        String adminName = getAdminIdentifier();
        payment.setStatus(PaymentStatus.APPROVED);
        payment.setValidatedBy(adminName);
        payment.setValidationDate(LocalDateTime.now());
        payment.setPaymentDate(LocalDate.now());

        SubscriptionEntity subscription = payment.getSubscription();
        if (subscription != null) {
            subscription.setStatus(SubscriptionStatus.ACTIVE);
            subscription.setStartDate(LocalDate.now());
            if (subscription.getPlan() != null) {
                subscription.setEndDate(LocalDate.now().plusDays(subscription.getPlan().getDurationDays()));
            }
            subscriptionRepository.save(subscription);
        }

        return responseMapper.convertToDto(paymentRepository.save(payment));
    }

    @Auditable(AuditActions.PROCESS_PAYMENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public PaymentResponseDto rejectCashPayment(UUID paymentPublicId) {
        PaymentEntity payment = paymentRepository.findByPublicId(paymentPublicId)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró el pago con ID: " + paymentPublicId));

        if (payment.getMethod() != PaymentMethods.EFECTIVO) {
            throw new IllegalStateException("Solo se pueden rechazar pagos registrados en efectivo.");
        }
        if (payment.getStatus() == PaymentStatus.APPROVED) {
            throw new IllegalStateException("No se puede rechazar un pago que ya fue validado y aprobado.");
        }

        String adminName = getAdminIdentifier();
        payment.setStatus(PaymentStatus.REJECTED);
        payment.setValidatedBy(adminName);
        payment.setValidationDate(LocalDateTime.now());

        SubscriptionEntity subscription = payment.getSubscription();
        if (subscription != null && subscription.getStatus() == SubscriptionStatus.PENDING) {
            subscription.setStatus(SubscriptionStatus.CANCELED);
            subscriptionRepository.save(subscription);
        }

        return responseMapper.convertToDto(paymentRepository.save(payment));
    }

    public List<PaymentResponseDto> getMyPayments() {
        assertNotAdminUser();
        UserEntity user = userService.getAuthenticatedUserEntity();
        return paymentRepository.findBySubscriptionUser(user)
                .stream()
                .map(responseMapper::convertToDto)
                .toList();
    }

    public List<PaymentResponseDto> getAllPayments() {
        return paymentRepository.findAll(Sort.by(Sort.Direction.DESC, "paymentDate", "id"))
                .stream()
                .map(responseMapper::convertToDto)
                .toList();
    }

    private String getAdminIdentifier() {
        try {
            UserEntity admin = userService.getAuthenticatedUserEntity();
            if (admin != null && admin.getEmail() != null) {
                return admin.getEmail();
            }
        } catch (Exception ignored) {
        }
        return "admin";
    }

    private void assertNotAdminUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getAuthorities() != null) {
            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
            if (isAdmin) {
                throw new IllegalStateException("Los administradores no pueden registrar pagos de membresías personales.");
            }
        }
    }
}
