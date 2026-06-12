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
    Optional<GymClassEntity> findByExternalId(UUID externalId);

    List<GymClassEntity>findByProfessorExternalId(UUID professorId);
    List<GymClassEntity>findByActivityExternalId(UUID activityId);
    List<GymClassEntity>findAllByDayOfWeek(DayOfWeek dayOfWeek);

    ///Verifico que no haya superposicion de clases(solapamiento(overlap))
    @Query("SELECT COUNT(c) > 0 FROM ClassEntity c WHERE c.professor.FirstName = :professorFirstName " +
            "AND c.dayOfWeek = :dayOfWeek " +
            "AND (:startTime < c.endTime AND :endTime > c.startTime)")
    boolean existsOverlap(@Param("professorFirstName") String professorUsername,
                          @Param("dayOfWeek") DayOfWeek dayOfWeek,
                          @Param("startTime") LocalTime startTime,
                          @Param("endTime") LocalTime endTime);

    @Query("SELECT COUNT(c) > 0 FROM ClassEntity c WHERE c.professor.FirstName = :professorFirstName " +
            "AND c.dayOfWeek = :dayOfWeek " +
            "AND c.externalId != :currentClassId " +
            "AND (:startTime < c.endTime AND :endTime > c.startTime)")
    boolean existsOverlapForUpdate(@Param("professorFirstName") String professorUsername,
                                   @Param("dayOfWeek") DayOfWeek dayOfWeek,
                                   @Param("startTime") LocalTime startTime,
                                   @Param("endTime") LocalTime endTime,
                                   @Param("currentClassId") UUID currentClassId);

}
