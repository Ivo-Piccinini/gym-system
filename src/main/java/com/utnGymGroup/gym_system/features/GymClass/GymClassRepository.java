package com.utnGymGroup.gym_system.features.GymClass;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GymClassRepository extends JpaRepository<GymClassEntity, Long> {
    Optional<GymClassEntity> findByExternalIdAndActiveTrue(UUID externalId);

    List<GymClassEntity> findByActiveTrue();

    List<GymClassEntity> findByProfessorExternalIdAndActiveTrue(UUID professorId);
    List<GymClassEntity> findByActivityExternalIdAndActiveTrue(UUID activityId);
    List<GymClassEntity> findAllByDayOfWeekAndActiveTrue(DayOfWeek dayOfWeek);

    @Query("SELECT COUNT(c) > 0 FROM GymClassEntity c WHERE c.active = true " +
            "AND c.professor.firstName = :professorFirstName " +
            "AND c.dayOfWeek = :dayOfWeek " +
            "AND (:startTime < c.endTime AND :endTime > c.startTime)")
    boolean existsOverlap(@Param("professorFirstName") String professorFirstName,
                          @Param("dayOfWeek") DayOfWeek dayOfWeek,
                          @Param("startTime") LocalTime startTime,
                          @Param("endTime") LocalTime endTime);

    @Query("SELECT COUNT(c) > 0 FROM GymClassEntity c WHERE c.active = true " +
            "AND c.professor.firstName = :professorFirstName " +
            "AND c.dayOfWeek = :dayOfWeek " +
            "AND c.externalId != :currentClassId " +
            "AND (:startTime < c.endTime AND :endTime > c.startTime)")
    boolean existsOverlapForUpdate(@Param("professorFirstName") String professorFirstName,
                                   @Param("dayOfWeek") DayOfWeek dayOfWeek,
                                   @Param("startTime") LocalTime startTime,
                                   @Param("endTime") LocalTime endTime,
                                   @Param("currentClassId") UUID currentClassId);
}