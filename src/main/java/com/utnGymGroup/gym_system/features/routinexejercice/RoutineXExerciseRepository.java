package com.utnGymGroup.gym_system.features.routinexejercice;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoutineXExerciseRepository extends JpaRepository<RoutineXExerciceEntity,Long>
{



}
