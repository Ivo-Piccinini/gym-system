package com.utnGymGroup.gym_system.features.enrollment;

import com.utnGymGroup.gym_system.features.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EnrollmentRepository extends JpaRepository<EnrollmentEntity, Long> {
    Optional<EnrollmentEntity> findByExternalId(UUID externalId);

    //saber cuántos alumnos hay en una clase específica
    long countByGymClassIdAndEnrollmentDate(Long classId, LocalDate date);

    //el cliente puede ver sus propias inscripciones
    List<EnrollmentEntity> findByClient(UserEntity client);

}