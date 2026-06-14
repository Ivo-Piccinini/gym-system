package com.utnGymGroup.gym_system.features.fullRoutine;
import com.utnGymGroup.gym_system.features.exercise.ExerciseEntity;
import com.utnGymGroup.gym_system.features.routine.RoutineEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table (name = "full_routine")
public class FullRoutineEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private UUID publicId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn (name = "routine_id",nullable = false)
    private RoutineEntity routine;

  @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id",nullable = false)
    private ExerciseEntity exercise;

  private Integer series;
  private Integer reps;
  private Double weight;

  @PrePersist
    private void generateUUID()
  {
      if(this.publicId == null)
      {
          setPublicId(UUID.randomUUID());
      }

  }

}
