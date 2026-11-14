package com.utnGymGroup.gym_system.features.payment;

import com.utnGymGroup.gym_system.features.membership.MembershipEntity;
import com.utnGymGroup.gym_system.features.payment.dtos.PaymentRequestDTO;
import com.utnGymGroup.gym_system.features.payment.dtos.PaymentResponseDto;
import com.utnGymGroup.gym_system.features.payment.mappers.PaymentRequestMapper;
import com.utnGymGroup.gym_system.features.payment.mappers.PaymentResponseMapper;
import com.utnGymGroup.gym_system.features.subscription.SubscriptionEntity;
import com.utnGymGroup.gym_system.features.subscription.SubscriptionRepository;
import com.utnGymGroup.gym_system.features.subscription.SubscriptionStatus;
import com.utnGymGroup.gym_system.features.user.UserEntity;
import com.utnGymGroup.gym_system.features.user.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentRequestMapper requestMapper;

    @Mock
    private PaymentResponseMapper responseMapper;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private PaymentService paymentService;

    private SubscriptionEntity subscription;
    private MembershipPlanMock plan;
    private UserEntity user;
    private UUID subscriptionId;
    private UUID paymentId;

    static class MembershipPlanMock extends MembershipEntity {
        MembershipPlanMock() {
            setDurationDays(30);
            setName("Plan Full");
            setPrice(25000.0);
        }
    }

    @BeforeEach
    void setUp() {
        subscriptionId = UUID.randomUUID();
        paymentId = UUID.randomUUID();

        user = UserEntity.builder()
                .email("socio@gym.com")
                .firstName("Juan")
                .lastName("Perez")
                .build();

        plan = new MembershipPlanMock();

        subscription = new SubscriptionEntity();
        subscription.setPublicId(subscriptionId);
        subscription.setUser(user);
        subscription.setPlan(plan);
        subscription.setStatus(SubscriptionStatus.PENDING);
        subscription.setStartDate(LocalDate.now());
        subscription.setEndDate(LocalDate.now().plusDays(30));
    }

    @Test
    @DisplayName("Debe crear pago con TARJETA aprobado de inmediato y activar suscripción")
    void createPayment_Card_ApprovedImmediately() {
        PaymentRequestDTO request = new PaymentRequestDTO(25000.0, LocalDate.now(), PaymentMethods.TARJETA, subscriptionId);

        when(subscriptionRepository.findByPublicId(subscriptionId)).thenReturn(Optional.of(subscription));
        when(paymentRepository.save(any(PaymentEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentResponseDto expectedDto = new PaymentResponseDto();
        expectedDto.setStatus(PaymentStatus.APPROVED);
        when(responseMapper.convertToDto(any(PaymentEntity.class))).thenReturn(expectedDto);

        PaymentResponseDto response = paymentService.createPayment(request);

        assertNotNull(response);
        assertEquals(PaymentStatus.APPROVED, response.getStatus());
        assertEquals(SubscriptionStatus.ACTIVE, subscription.getStatus());
        verify(subscriptionRepository).save(subscription);
        verify(paymentRepository).save(any(PaymentEntity.class));
    }

    @Test
    @DisplayName("Debe crear pago con EFECTIVO en estado PENDING y mantener suscripción PENDING")
    void createPayment_Cash_PendingApproval() {
        PaymentRequestDTO request = new PaymentRequestDTO(25000.0, LocalDate.now(), PaymentMethods.EFECTIVO, subscriptionId);

        when(subscriptionRepository.findByPublicId(subscriptionId)).thenReturn(Optional.of(subscription));
        when(paymentRepository.save(any(PaymentEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentResponseDto expectedDto = new PaymentResponseDto();
        expectedDto.setStatus(PaymentStatus.PENDING);
        when(responseMapper.convertToDto(any(PaymentEntity.class))).thenReturn(expectedDto);

        PaymentResponseDto response = paymentService.createPayment(request);

        assertNotNull(response);
        assertEquals(PaymentStatus.PENDING, response.getStatus());
        assertEquals(SubscriptionStatus.PENDING, subscription.getStatus());
        verify(subscriptionRepository).save(subscription);
        verify(paymentRepository).save(any(PaymentEntity.class));
    }

    @Test
    @DisplayName("Debe validar pago en EFECTIVO pendiente, aprobarlo y activar suscripción")
    void validateCashPayment_Success() {
        PaymentEntity payment = new PaymentEntity();
        payment.setPublicId(paymentId);
        payment.setMethod(PaymentMethods.EFECTIVO);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setAmount(25000.0);
        payment.setSubscription(subscription);

        UserEntity admin = UserEntity.builder().email("admin@gym.com").build();
        when(userService.getAuthenticatedUserEntity()).thenReturn(admin);
        when(paymentRepository.findByPublicId(paymentId)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(PaymentEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentResponseDto expectedDto = new PaymentResponseDto();
        expectedDto.setStatus(PaymentStatus.APPROVED);
        expectedDto.setValidatedBy("admin@gym.com");
        when(responseMapper.convertToDto(any(PaymentEntity.class))).thenReturn(expectedDto);

        PaymentResponseDto result = paymentService.validateCashPayment(paymentId);

        assertNotNull(result);
        assertEquals(PaymentStatus.APPROVED, payment.getStatus());
        assertEquals("admin@gym.com", payment.getValidatedBy());
        assertEquals(SubscriptionStatus.ACTIVE, subscription.getStatus());
        assertNotNull(payment.getValidationDate());
        verify(subscriptionRepository).save(subscription);
        verify(paymentRepository).save(payment);
    }

    @Test
    @DisplayName("Debe lanzar excepción si el pago no existe")
    void validateCashPayment_NotFound() {
        when(paymentRepository.findByPublicId(paymentId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> paymentService.validateCashPayment(paymentId));
    }

    @Test
    @DisplayName("Debe lanzar excepción al intentar validar pago que no es EFECTIVO")
    void validateCashPayment_NotCash_ThrowsException() {
        PaymentEntity payment = new PaymentEntity();
        payment.setPublicId(paymentId);
        payment.setMethod(PaymentMethods.TARJETA);
        payment.setStatus(PaymentStatus.APPROVED);

        when(paymentRepository.findByPublicId(paymentId)).thenReturn(Optional.of(payment));

        assertThrows(IllegalStateException.class, () -> paymentService.validateCashPayment(paymentId));
    }

    @Test
    @DisplayName("Debe lanzar excepción al intentar validar pago ya aprobado")
    void validateCashPayment_AlreadyApproved_ThrowsException() {
        PaymentEntity payment = new PaymentEntity();
        payment.setPublicId(paymentId);
        payment.setMethod(PaymentMethods.EFECTIVO);
        payment.setStatus(PaymentStatus.APPROVED);

        when(paymentRepository.findByPublicId(paymentId)).thenReturn(Optional.of(payment));

        assertThrows(IllegalStateException.class, () -> paymentService.validateCashPayment(paymentId));
    }

    @Test
    @DisplayName("Debe rechazar pago en efectivo pendiente y cancelar suscripción")
    void rejectCashPayment_Success() {
        PaymentEntity payment = new PaymentEntity();
        payment.setPublicId(paymentId);
        payment.setMethod(PaymentMethods.EFECTIVO);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setSubscription(subscription);

        UserEntity admin = UserEntity.builder().email("admin@gym.com").build();
        when(userService.getAuthenticatedUserEntity()).thenReturn(admin);
        when(paymentRepository.findByPublicId(paymentId)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(PaymentEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentResponseDto expectedDto = new PaymentResponseDto();
        expectedDto.setStatus(PaymentStatus.REJECTED);
        when(responseMapper.convertToDto(any(PaymentEntity.class))).thenReturn(expectedDto);

        PaymentResponseDto result = paymentService.rejectCashPayment(paymentId);

        assertNotNull(result);
        assertEquals(PaymentStatus.REJECTED, payment.getStatus());
        assertEquals(SubscriptionStatus.CANCELED, subscription.getStatus());
        verify(subscriptionRepository).save(subscription);
        verify(paymentRepository).save(payment);
    }
}
