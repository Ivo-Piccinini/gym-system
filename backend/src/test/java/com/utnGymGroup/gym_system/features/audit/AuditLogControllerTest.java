package com.utnGymGroup.gym_system.features.audit;

import com.utnGymGroup.gym_system.features.audit.dtos.AuditLogResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditLogControllerTest {

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private AuditLogController auditLogController;

    @Test
    @DisplayName("GET /api/audit/logs delega al servicio y retorna 200 OK con la bitácora")
    void shouldReturnAuditLogsWithStatus200() {
        AuditLogResponseDto dto = AuditLogResponseDto.builder()
                .id(10L)
                .action(AuditActions.UPDATE_PLAN)
                .performedBy("admin")
                .timestamp(LocalDateTime.now())
                .details("[{\"price\":22000.0}]")
                .build();

        when(auditLogService.getAllAuditLogs()).thenReturn(List.of(dto));

        ResponseEntity<List<AuditLogResponseDto>> response = auditLogController.getAuditLogs();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(AuditActions.UPDATE_PLAN, response.getBody().get(0).getAction());
        verify(auditLogService, times(1)).getAllAuditLogs();
    }
}
