package com.utnGymGroup.gym_system.features.access;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessPassDTO {
    private UUID publicId;
    private UUID userPublicId;
    private String clientName;
    private String clientDni;
    private String clientEmail;
    private String planName;
    private String accessCode;
    private String qrData;
    private String qrImageBase64;
    private String generatedBy;
    private LocalDateTime generatedAt;
    private LocalDate validUntil;
    private boolean active;
}
