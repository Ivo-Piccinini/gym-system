package com.utnGymGroup.gym_system.features.exercise;


import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Table(name = "Exercise")
public class ExerciseEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,unique = true,updatable = false)
    private Long idPublic;

    @Column(unique = true, nullable = true)
    private String name;

    @Column(name = "description")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "muscle_group")
    private MuscleGroup muscleGroup;///Podria ser enum tambien

    private Boolean enabled = true;

    @PrePersist
    protected void onCreate(){
        if(this.idPublic == null) {
            this.idPublic = ThreadLocalRandom.current().nextLong(10_000_000L, 100_000_000L);
        }
    }
    
}
