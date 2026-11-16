package com.utnGymGroup.gym_system.features.access;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.utnGymGroup.gym_system.features.audit.AuditActions;
import com.utnGymGroup.gym_system.features.audit.Auditable;
import com.utnGymGroup.gym_system.features.subscription.SubscriptionEntity;
import com.utnGymGroup.gym_system.features.subscription.SubscriptionRepository;
import com.utnGymGroup.gym_system.features.subscription.SubscriptionStatus;
import com.utnGymGroup.gym_system.features.user.UserEntity;
import com.utnGymGroup.gym_system.features.user.UserRepository;
import com.utnGymGroup.gym_system.features.user.UserService;
import com.utnGymGroup.gym_system.features.user.exceptions.UserNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AccessPassService {

    private final AccessPassRepository accessPassRepository;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserService userService;
    private final QrCodeGeneratorService qrCodeGeneratorService;
    private final ObjectMapper objectMapper;

    @Auditable(AuditActions.GENERATE_ACCESS_QR)
    @Transactional
    public AccessPassDTO generateAccessPass(UUID userPublicId) {
        UserEntity client = userRepository.findByPublicId(userPublicId)
                .orElseThrow(() -> new UserNotFoundException("Socio no encontrado con ID público: " + userPublicId));

        List<SubscriptionEntity> userSubscriptions = subscriptionRepository.findByUser(client);
        LocalDate today = LocalDate.now();

        SubscriptionEntity activeSub = userSubscriptions.stream()
                .filter(s -> s.getStatus() == SubscriptionStatus.ACTIVE &&
                        (s.getEndDate() == null || !s.getEndDate().isBefore(today)))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No se puede generar el código QR: el socio no posee una membresía activa con pago verificado."
                ));

        // Desactivar pases previos del socio
        List<AccessPassEntity> existingPasses = accessPassRepository.findByUser(client);
        for (AccessPassEntity prev : existingPasses) {
            prev.setActive(false);
        }
        if (!existingPasses.isEmpty()) {
            accessPassRepository.saveAll(existingPasses);
        }

        String accessCode = "GYM-PASS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        LocalDate validUntil = activeSub.getEndDate() != null ? activeSub.getEndDate() : today.plusDays(30);

        Map<String, Object> payloadMap = new LinkedHashMap<>();
        payloadMap.put("gym", "Gym System");
        payloadMap.put("passCode", accessCode);
        payloadMap.put("clientDni", client.getDni());
        payloadMap.put("clientName", client.getFirstName() + " " + client.getLastName());
        payloadMap.put("plan", activeSub.getPlan() != null ? activeSub.getPlan().getName() : "Membresía");
        payloadMap.put("validUntil", validUntil.toString());
        payloadMap.put("generatedAt", LocalDateTime.now().toString());

        String qrData;
        try {
            qrData = objectMapper.writeValueAsString(payloadMap);
        } catch (Exception e) {
            qrData = "GYM:" + accessCode + ":" + client.getDni() + ":" + validUntil;
        }

        String qrBase64 = qrCodeGeneratorService.generateQrBase64(qrData, 300, 300);
        String adminUsername = getAuthenticatedAdminIdentifier();

        AccessPassEntity passEntity = AccessPassEntity.builder()
                .publicId(UUID.randomUUID())
                .user(client)
                .subscription(activeSub)
                .accessCode(accessCode)
                .qrData(qrData)
                .qrImageBase64(qrBase64)
                .generatedBy(adminUsername)
                .generatedAt(LocalDateTime.now())
                .validUntil(validUntil)
                .active(true)
                .build();

        AccessPassEntity saved = accessPassRepository.save(passEntity);
        return mapToDto(saved);
    }

    @Transactional(readOnly = true)
    public AccessPassDTO getActivePassByUser(UUID userPublicId) {
        UserEntity client = userRepository.findByPublicId(userPublicId)
                .orElseThrow(() -> new UserNotFoundException("Socio no encontrado con ID público: " + userPublicId));

        return accessPassRepository.findFirstByUserAndActiveTrueOrderByGeneratedAtDesc(client)
                .map(this::mapToDto)
                .orElseThrow(() -> new EntityNotFoundException("El socio no posee un pase QR de acceso activo emitido."));
    }

    @Transactional(readOnly = true)
    public AccessPassDTO getMyActivePass() {
        UserEntity currentUser = userService.getAuthenticatedUserEntity();

        return accessPassRepository.findFirstByUserAndActiveTrueOrderByGeneratedAtDesc(currentUser)
                .map(this::mapToDto)
                .orElseThrow(() -> new EntityNotFoundException("No poseés un pase QR de acceso activo. Solicitá su emisión en recepción."));
    }

    private AccessPassDTO mapToDto(AccessPassEntity entity) {
        UserEntity user = entity.getUser();
        String planName = (entity.getSubscription() != null && entity.getSubscription().getPlan() != null)
                ? entity.getSubscription().getPlan().getName()
                : "Membresía";

        return AccessPassDTO.builder()
                .publicId(entity.getPublicId())
                .userPublicId(user.getPublicId())
                .clientName(user.getFirstName() + " " + user.getLastName())
                .clientDni(user.getDni())
                .clientEmail(user.getEmail())
                .planName(planName)
                .accessCode(entity.getAccessCode())
                .qrData(entity.getQrData())
                .qrImageBase64(entity.getQrImageBase64())
                .generatedBy(entity.getGeneratedBy())
                .generatedAt(entity.getGeneratedAt())
                .validUntil(entity.getValidUntil())
                .active(entity.isActive())
                .build();
    }

    private String getAuthenticatedAdminIdentifier() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null && !auth.getName().isBlank()) {
            return auth.getName();
        }
        return "admin";
    }
}
