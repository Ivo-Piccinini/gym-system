package com.utnGymGroup.gym_system.features.fulllRoutine;
import com.utnGymGroup.gym_system.features.exercise.ExerciseEntity;
import com.utnGymGroup.gym_system.features.routine.RoutineEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table (name = "full_routines")
public class FullRoutineEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private UUID publicId;

  @ManyToOne
  @JoinColumn (name = "routine_id")
    private RoutineEntity routine;

  @ManyToOne
    @JoinColumn(name = "exercise_id")
    private ExerciseEntity exercise;

  private Integer series;
  private Integer reps;
  private Integer weight;

  @PrePersist
    public void generateUUID()
  {
      if(publicId == null)
      {
          setPublicId(UUID.randomUUID());
      }

  }

}
