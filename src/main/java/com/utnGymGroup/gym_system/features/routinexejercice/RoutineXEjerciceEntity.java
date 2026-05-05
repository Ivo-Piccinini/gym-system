package com.utnGymGroup.gym_system.features.routinexejercice;
import com.utnGymGroup.gym_system.features.exercise.ExerciseEntity;
import com.utnGymGroup.gym_system.features.routine.RoutineEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table (name = "routineXejercice")
public class RoutineXEjerciceEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

  @ManyToOne
  @JoinColumn (name = "routine_id")
    private RoutineEntity routine;

  @ManyToOne
    @JoinColumn(name = "exercise_id")
    private ExerciseEntity exercise;

  private Integer series;
  private Integer reps;
  private Integer weight;

}
