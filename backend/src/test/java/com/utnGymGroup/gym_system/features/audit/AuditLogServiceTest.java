package com.utnGymGroup.gym_system.features.audit;

import com.utnGymGroup.gym_system.features.audit.dtos.AuditLogResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditLogService auditLogService;

    @Test
    @DisplayName("getAllAuditLogs debe retornar lista de DTOs ordenados cronológicamente descendente")
    void shouldReturnAllAuditLogsMappedToDto() {
        LocalDateTime now = LocalDateTime.now();
        AuditLogEntity entity1 = AuditLogEntity.builder()
                .id(1L)
                .action(AuditActions.UPDATE_PLAN)
                .performedBy("admin")
                .timestamp(now)
                .details("[{\"price\":25000.0}]")
                .build();

        AuditLogEntity entity2 = AuditLogEntity.builder()
                .id(2L)
                .action(AuditActions.CREATE_PLAN)
                .performedBy("admin")
                .timestamp(now.minusHours(1))
                .details("[{\"price\":20000.0}]")
                .build();

        when(auditLogRepository.findAllByOrderByTimestampDesc()).thenReturn(List.of(entity1, entity2));

        List<AuditLogResponseDto> result = auditLogService.getAllAuditLogs();

        assertEquals(2, result.size());
        assertEquals(AuditActions.UPDATE_PLAN, result.get(0).getAction());
        assertEquals("admin", result.get(0).getPerformedBy());
        assertEquals("[{\"price\":25000.0}]", result.get(0).getDetails());
        assertEquals(1L, result.get(0).getId());

        assertEquals(AuditActions.CREATE_PLAN, result.get(1).getAction());
        verify(auditLogRepository, times(1)).findAllByOrderByTimestampDesc();
    }
}
