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

    //cupos por fecha
    long countByGymClassIdAndEnrollmentDate(Long classId, LocalDate date);

    long countByGymClassId(Long classId);

    List<EnrollmentEntity> findByClient(UserEntity client);

}