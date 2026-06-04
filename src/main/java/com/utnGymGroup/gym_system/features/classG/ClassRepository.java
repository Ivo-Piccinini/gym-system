package com.utnGymGroup.gym_system.features.classG;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClassRepository extends JpaRepository<ClassEntity, Long> {
    Optional<ClassEntity> findByExternalId(UUID externalId);

    List<ClassEntity>findByProfessorExternalId(UUID professorId);
    List<ClassEntity>findByActivityExternalId(UUID activityId);
    List<ClassEntity>findAllByDayOfWeek(DayOfWeek dayOfWeek);

    ///Verifico que no haya superposicion de clases(solapamiento(overlap))
    @Query("SELECT COUNT(c) > 0 FROM ClassEntity c WHERE c.professor.username = :professorUsername " +
            "AND c.dayOfWeek = :dayOfWeek " +
            "AND (:startTime < c.endTime AND :endTime > c.startTime)")
    boolean existsOverlap(@Param("professorUsername") String professorUsername,
                          @Param("dayOfWeek") DayOfWeek dayOfWeek,
                          @Param("startTime") LocalTime startTime,
                          @Param("endTime") LocalTime endTime);

    @Query("SELECT COUNT(c) > 0 FROM ClassEntity c WHERE c.professor.username = :professorUsername " +
            "AND c.dayOfWeek = :dayOfWeek " +
            "AND c.externalId != :currentClassId " +
            "AND (:startTime < c.endTime AND :endTime > c.startTime)")
    boolean existsOverlapForUpdate(@Param("professorUsername") String professorUsername,
                                   @Param("dayOfWeek") DayOfWeek dayOfWeek,
                                   @Param("startTime") LocalTime startTime,
                                   @Param("endTime") LocalTime endTime,
                                   @Param("currentClassId") UUID currentClassId);

}
