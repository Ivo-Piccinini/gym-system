package com.utnGymGroup.gym_system.features.audit;

import com.utnGymGroup.gym_system.features.audit.dtos.AuditLogResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public List<AuditLogResponseDto> getAllAuditLogs() {
        return auditLogRepository.findAllByOrderByTimestampDesc()
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    private AuditLogResponseDto mapToDto(AuditLogEntity entity) {
        return AuditLogResponseDto.builder()
                .id(entity.getId())
                .action(entity.getAction())
                .performedBy(entity.getPerformedBy())
                .timestamp(entity.getTimestamp())
                .details(entity.getDetails())
                .build();
    }
}
