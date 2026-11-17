package com.utnGymGroup.gym_system.features.GymClass;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.utnGymGroup.gym_system.features.enrollment.EnrollmentRepository;

@ExtendWith(MockitoExtension.class)
class GymClassServiceTest {

    @Mock
    private GymClassRepository gymClassRepository;

    @Mock
    private GymClassMapper gymClassMapper;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @InjectMocks
    private GymClassService gymClassService;

    private GymClassEntity testClassEntity;
    private GymClassDTO testClassDTO;

    @BeforeEach
    void setUp() {
        testClassEntity = new GymClassEntity();
        testClassEntity.setId(10L);
        testClassEntity.setExternalId(UUID.randomUUID());
        testClassEntity.setCapacityMax(20);
        testClassEntity.setDayOfWeek(DayOfWeek.MONDAY);
        testClassEntity.setStartTime(LocalTime.of(10, 0));
        testClassEntity.setEndTime(LocalTime.of(11, 0));
        testClassEntity.setActive(true);

        testClassDTO = new GymClassDTO();
        testClassDTO.setExternalId(testClassEntity.getExternalId());
        testClassDTO.setCapacityMax(20);
        testClassDTO.setDayOfWeek(DayOfWeek.MONDAY);
        testClassDTO.setStartTime(LocalTime.of(10, 0));
        testClassDTO.setEndTime(LocalTime.of(11, 0));
    }

    @Test
    @DisplayName("getAllClasses calcula enrolledCount y availableSpots correctamente")
    void shouldCalculateEnrolledAndAvailableSpotsForDate() {
        LocalDate testDate = LocalDate.of(2026, 11, 10);
        when(gymClassRepository.findByActiveTrue()).thenReturn(List.of(testClassEntity));
        when(gymClassMapper.convertToDto(testClassEntity)).thenReturn(testClassDTO);
        when(enrollmentRepository.countByGymClassId(10L)).thenReturn(7L);

        List<GymClassDTO> result = gymClassService.getAllClasses(testDate);

        assertNotNull(result);
        assertEquals(1, result.size());
        GymClassDTO item = result.get(0);
        assertEquals(7, item.getEnrolledCount());
        assertEquals(13, item.getAvailableSpots());
        verify(enrollmentRepository).countByGymClassId(10L);
    }

    @Test
    @DisplayName("getAllClasses sin fecha calcula enrolledCount y availableSpots correctamente")
    void shouldDefaultToCurrentDateWhenDateIsNull() {
        when(gymClassRepository.findByActiveTrue()).thenReturn(List.of(testClassEntity));
        when(gymClassMapper.convertToDto(testClassEntity)).thenReturn(testClassDTO);
        when(enrollmentRepository.countByGymClassId(10L)).thenReturn(3L);

        List<GymClassDTO> result = gymClassService.getAllClasses((LocalDate) null);

        assertNotNull(result);
        assertEquals(1, result.size());
        GymClassDTO item = result.get(0);
        assertEquals(3, item.getEnrolledCount());
        assertEquals(17, item.getAvailableSpots());
    }

    @Test
    @DisplayName("availableSpots no debe ser negativo si los inscriptos exceden la capacidad máxima")
    void shouldClampAvailableSpotsToZeroWhenOverenrolled() {
        LocalDate testDate = LocalDate.of(2026, 11, 10);
        when(gymClassRepository.findByActiveTrue()).thenReturn(List.of(testClassEntity));
        when(gymClassMapper.convertToDto(testClassEntity)).thenReturn(testClassDTO);
        when(enrollmentRepository.countByGymClassId(10L)).thenReturn(25L);

        List<GymClassDTO> result = gymClassService.getAllClasses(testDate);

        assertNotNull(result);
        GymClassDTO item = result.get(0);
        assertEquals(25, item.getEnrolledCount());
        assertEquals(0, item.getAvailableSpots());
    }

    @Test
    @DisplayName("Sobrecarga getAllClasses sin argumentos delega al cálculo de cupos vigentes")
    void shouldSupportNoArgGetAllClasses() {
        when(gymClassRepository.findByActiveTrue()).thenReturn(List.of(testClassEntity));
        when(gymClassMapper.convertToDto(testClassEntity)).thenReturn(testClassDTO);
        when(enrollmentRepository.countByGymClassId(10L)).thenReturn(0L);

        List<GymClassDTO> result = gymClassService.getAllClasses();

        assertNotNull(result);
        assertEquals(20, result.get(0).getAvailableSpots());
        assertEquals(0, result.get(0).getEnrolledCount());
    }
}

