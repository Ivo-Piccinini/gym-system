package com.utnGymGroup.gym_system.features.enrollment;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.utnGymGroup.gym_system.features.GymClass.GymClassEntity;
import com.utnGymGroup.gym_system.features.GymClass.GymClassRepository;
import com.utnGymGroup.gym_system.features.user.UserEntity;
import com.utnGymGroup.gym_system.features.user.UserService;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private EnrollmentMapper enrollmentMapper;

    @Mock
    private GymClassRepository gymClassRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private EnrollmentService enrollmentService;

    private UserEntity clientUser;
    private GymClassEntity gymClass;
    private UUID classExternalId;

    @BeforeEach
    void setUp() {
        classExternalId = UUID.randomUUID();

        clientUser = UserEntity.builder()
                .id(1L)
                .firstName("Socio")
                .lastName("Prueba")
                .email("socio@gym.com")
                .dni("12345678")
                .build();

        gymClass = new GymClassEntity();
        gymClass.setId(10L);
        gymClass.setExternalId(classExternalId);
        gymClass.setCapacityMax(20);
        gymClass.setActive(true);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateWithRole(String role) {
        var auth = new UsernamePasswordAuthenticationToken(
                "userTest",
                null,
                List.of(new SimpleGrantedAuthority(role))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("Debe denegar la inscripción a una clase si el usuario posee rol ROLE_ADMIN")
    void enrollClient_throwsIllegalStateException_whenUserIsAdmin() {
        authenticateWithRole("ROLE_ADMIN");

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                enrollmentService.enrollClient(classExternalId)
        );

        assertTrue(exception.getMessage().contains("Los administradores no pueden inscribirse a clases"));
        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe rechazar la obtención de mis inscripciones si el usuario posee rol ROLE_ADMIN")
    void getMyEnrollments_throwsIllegalStateException_whenUserIsAdmin() {
        authenticateWithRole("ROLE_ADMIN");

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                enrollmentService.getMyEnrollments()
        );

        assertTrue(exception.getMessage().contains("Los administradores no pueden inscribirse a clases"));
        verify(enrollmentRepository, never()).findByClient(any());
    }

    @Test
    @DisplayName("Debe rechazar la inscripción si la clase alcanzó su cupo máximo")
    void enrollClient_throwsIllegalStateException_whenClassIsFull() {
        authenticateWithRole("ROLE_CLIENT");
        when(userService.getAuthenticatedUserEntity()).thenReturn(clientUser);
        when(gymClassRepository.findByExternalIdAndActiveTrue(classExternalId)).thenReturn(Optional.of(gymClass));
        when(enrollmentRepository.countByGymClassId(gymClass.getId())).thenReturn(20L);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                enrollmentService.enrollClient(classExternalId)
        );

        assertTrue(exception.getMessage().contains("capacidad máxima"));
        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe inscribir exitosamente a un socio con ROLE_CLIENT cuando hay cupos disponibles")
    void enrollClient_success_whenUserIsClientAndSpotsAvailable() {
        authenticateWithRole("ROLE_CLIENT");
        when(userService.getAuthenticatedUserEntity()).thenReturn(clientUser);
        when(gymClassRepository.findByExternalIdAndActiveTrue(classExternalId)).thenReturn(Optional.of(gymClass));
        when(enrollmentRepository.countByGymClassId(gymClass.getId())).thenReturn(5L);

        EnrollmentEntity savedEntity = new EnrollmentEntity();
        savedEntity.setId(100L);
        savedEntity.setClient(clientUser);
        savedEntity.setGymClass(gymClass);
        savedEntity.setEnrollmentDate(LocalDate.now());

        EnrollmentDTO expectedDto = new EnrollmentDTO();
        expectedDto.setEnrollmentDate(LocalDate.now());

        when(enrollmentRepository.save(any(EnrollmentEntity.class))).thenReturn(savedEntity);
        when(enrollmentMapper.convertToDto(savedEntity)).thenReturn(expectedDto);

        EnrollmentDTO result = enrollmentService.enrollClient(classExternalId);

        assertNotNull(result);
        verify(enrollmentRepository).save(any(EnrollmentEntity.class));
    }

    @Test
    @DisplayName("Debe impedir cancelar una inscripción que no pertenece al usuario autenticado")
    void cancelEnrollment_throwsIllegalStateException_whenEnrollmentNotOwned() {
        UUID enrollmentUuid = UUID.randomUUID();

        UserEntity anotherUser = UserEntity.builder()
                .id(99L)
                .email("otro@gym.com")
                .build();

        EnrollmentEntity foreignEnrollment = new EnrollmentEntity();
        foreignEnrollment.setId(50L);
        foreignEnrollment.setExternalId(enrollmentUuid);
        foreignEnrollment.setClient(anotherUser);

        when(enrollmentRepository.findByExternalId(enrollmentUuid)).thenReturn(Optional.of(foreignEnrollment));
        when(userService.getAuthenticatedUserEntity()).thenReturn(clientUser); // id 1 != 99

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                enrollmentService.cancelEnrollment(enrollmentUuid)
        );

        assertTrue(exception.getMessage().contains("No tienes permiso para cancelar esta inscripción"));
        verify(enrollmentRepository, never()).delete(any());
    }
}
