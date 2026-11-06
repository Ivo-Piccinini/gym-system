package com.utnGymGroup.gym_system.features.GymClass;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GymClassControllerTest {

    @Mock
    private GymClassService gymClassService;

    @InjectMocks
    private GymClassController gymClassController;

    @Test
    @DisplayName("GET /api/gym-classes?date=YYYY-MM-DD delega al servicio y retorna 200 OK con cupos")
    void shouldReturnClassesWithDateParameter() {
        LocalDate date = LocalDate.of(2026, 11, 15);
        GymClassDTO dto = new GymClassDTO();
        dto.setExternalId(UUID.randomUUID());
        dto.setCapacityMax(25);
        dto.setEnrolledCount(5);
        dto.setAvailableSpots(20);

        when(gymClassService.getAllClasses(date)).thenReturn(List.of(dto));

        ResponseEntity<List<GymClassDTO>> response = gymClassController.getAllGymClasses(date);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(20, response.getBody().get(0).getAvailableSpots());
        verify(gymClassService).getAllClasses(date);
    }

    @Test
    @DisplayName("GET /api/gym-classes sin parámetro date delega con null al servicio")
    void shouldReturnClassesWithoutDateParameter() {
        GymClassDTO dto = new GymClassDTO();
        dto.setExternalId(UUID.randomUUID());
        dto.setCapacityMax(15);
        dto.setEnrolledCount(2);
        dto.setAvailableSpots(13);

        when(gymClassService.getAllClasses(null)).thenReturn(List.of(dto));

        ResponseEntity<List<GymClassDTO>> response = gymClassController.getAllGymClasses(null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(13, response.getBody().get(0).getAvailableSpots());
        verify(gymClassService).getAllClasses(null);
    }
}

