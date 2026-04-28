package com.utnGymGroup.gym_system.feature.routinexejercice;
import com.utnGymGroup.gym_system.feature.exercise.ExerciseEntity;
import com.utnGymGroup.gym_system.feature.routine.RoutineEntity;
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
    private Integer id;

  @ManyToOne
  @JoinColumn (name = "routine_id")
    private RoutineEntity routine;

  @ManyToOne
    @JoinColumn(name = "Exercise_id")
    private ExerciseEntity exercise;

  private Integer series;
  private Integer reps;
  private Integer weight;

}
