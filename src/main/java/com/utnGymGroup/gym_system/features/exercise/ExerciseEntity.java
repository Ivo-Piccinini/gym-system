package com.utnGymGroup.gym_system.features.exercise;


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
@Table(name = "Exercise")
public class ExerciseEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,unique = true,updatable = false)
    private String idPublic;

    @Column(unique = true, nullable = true)
    private String name;
    private String descripcion;
    private String muscle_group;///Podria ser enum tambien

    private Boolean enabled = true;

    @PrePersist
    void onCreate(){
        if(this.idPublic == null)
            this.idPublic=java.util.UUID.randomUUID().toString();
    }
    
}
