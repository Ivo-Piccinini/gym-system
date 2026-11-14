package com.utnGymGroup.gym_system.features.access;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.utnGymGroup.gym_system.features.membership.MembershipEntity;
import com.utnGymGroup.gym_system.features.subscription.SubscriptionEntity;
import com.utnGymGroup.gym_system.features.subscription.SubscriptionRepository;
import com.utnGymGroup.gym_system.features.subscription.SubscriptionStatus;
import com.utnGymGroup.gym_system.features.user.UserEntity;
import com.utnGymGroup.gym_system.features.user.UserRepository;
import com.utnGymGroup.gym_system.features.user.UserService;
import com.utnGymGroup.gym_system.features.user.exceptions.UserNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccessPassServiceTest {

    @Mock
    private AccessPassRepository accessPassRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private UserService userService;

    @Mock
    private QrCodeGeneratorService qrCodeGeneratorService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private AccessPassService accessPassService;

    private UserEntity clientUser;
    private SubscriptionEntity activeSubscription;
    private UUID userPublicId;

    @BeforeEach
    void setUp() {
        userPublicId = UUID.randomUUID();

        clientUser = UserEntity.builder()
                .id(1L)
                .publicId(userPublicId)
                .firstName("Martín")
                .lastName("Palermo")
                .dni("12345678")
                .email("martin@gym.com")
                .build();

        MembershipEntity planEntity = new MembershipEntity();
        planEntity.setId(10L);
        planEntity.setName("Plan Trimestral");
        planEntity.setDurationDays(90);

        activeSubscription = new SubscriptionEntity();
        activeSubscription.setId(100L);
        activeSubscription.setPublicId(UUID.randomUUID());
        activeSubscription.setUser(clientUser);
        activeSubscription.setPlan(planEntity);
        activeSubscription.setStatus(SubscriptionStatus.ACTIVE);
        activeSubscription.setStartDate(LocalDate.now());
        activeSubscription.setEndDate(LocalDate.now().plusDays(90));

        var auth = new UsernamePasswordAuthenticationToken(
                "admin", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Debe generar exitosamente el pase QR de acceso para un socio con membresía ACTIVE")
    void generateAccessPass_success_whenClientHasActiveSubscription() {
        when(userRepository.findByPublicId(userPublicId)).thenReturn(Optional.of(clientUser));
        when(subscriptionRepository.findByUser(clientUser)).thenReturn(List.of(activeSubscription));
        when(accessPassRepository.findByUser(clientUser)).thenReturn(Collections.emptyList());
        when(qrCodeGeneratorService.generateQrBase64(anyString(), eq(300), eq(300)))
                .thenReturn("data:image/png;base64,mockQrBase64Data");
        when(accessPassRepository.save(any(AccessPassEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AccessPassDTO result = accessPassService.generateAccessPass(userPublicId);

        assertNotNull(result);
        assertEquals(userPublicId, result.getUserPublicId());
        assertEquals("Martín Palermo", result.getClientName());
        assertEquals("12345678", result.getClientDni());
        assertEquals("Plan Trimestral", result.getPlanName());
        assertTrue(result.getAccessCode().startsWith("GYM-PASS-"));
        assertEquals("data:image/png;base64,mockQrBase64Data", result.getQrImageBase64());
        assertEquals("admin", result.getGeneratedBy());
        assertTrue(result.isActive());

        verify(accessPassRepository).save(any(AccessPassEntity.class));
    }

    @Test
    @DisplayName("Debe fallar al generar el QR si el socio no tiene ninguna suscripción activa")
    void generateAccessPass_throwsIllegalStateException_whenNoActiveSubscription() {
        SubscriptionEntity pendingSub = new SubscriptionEntity();
        pendingSub.setStatus(SubscriptionStatus.PENDING);

        when(userRepository.findByPublicId(userPublicId)).thenReturn(Optional.of(clientUser));
        when(subscriptionRepository.findByUser(clientUser)).thenReturn(List.of(pendingSub));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                accessPassService.generateAccessPass(userPublicId)
        );

        assertTrue(ex.getMessage().contains("No se puede generar el código QR"));
        verify(accessPassRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe fallar si el socio no existe en el sistema")
    void generateAccessPass_throwsUserNotFoundException_whenUserNotFound() {
        when(userRepository.findByPublicId(userPublicId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () ->
                accessPassService.generateAccessPass(userPublicId)
        );

        verify(accessPassRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe devolver el pase QR activo del socio autenticado")
    void getMyActivePass_success_whenActivePassExists() {
        when(userService.getAuthenticatedUserEntity()).thenReturn(clientUser);

        AccessPassEntity passEntity = AccessPassEntity.builder()
                .publicId(UUID.randomUUID())
                .user(clientUser)
                .subscription(activeSubscription)
                .accessCode("GYM-PASS-ABC12345")
                .qrData("{\"gym\":\"Gym System\"}")
                .qrImageBase64("data:image/png;base64,existingQr")
                .generatedBy("admin")
                .generatedAt(LocalDateTime.now())
                .validUntil(LocalDate.now().plusDays(90))
                .active(true)
                .build();

        when(accessPassRepository.findFirstByUserAndActiveTrueOrderByGeneratedAtDesc(clientUser))
                .thenReturn(Optional.of(passEntity));

        AccessPassDTO result = accessPassService.getMyActivePass();

        assertNotNull(result);
        assertEquals("GYM-PASS-ABC12345", result.getAccessCode());
        assertEquals("data:image/png;base64,existingQr", result.getQrImageBase64());
        assertTrue(result.isActive());
    }

    @Test
    @DisplayName("Debe arrojar EntityNotFoundException si el socio no tiene pase QR activo")
    void getMyActivePass_throwsEntityNotFoundException_whenNoActivePass() {
        when(userService.getAuthenticatedUserEntity()).thenReturn(clientUser);
        when(accessPassRepository.findFirstByUserAndActiveTrueOrderByGeneratedAtDesc(clientUser))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                accessPassService.getMyActivePass()
        );
    }
}
