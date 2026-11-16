package com.utnGymGroup.gym_system.features.audit.dtos;

import com.utnGymGroup.gym_system.features.audit.AuditActions;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogResponseDto {
    private Long id;
    private AuditActions action;
    private String performedBy;
    private LocalDateTime timestamp;
    private String details;
}
