package com.utnGymGroup.gym_system.features.routine;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "routine")

public class RoutineEntity
{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true,updatable = false)
    private Long publicId;

    private String name;
    private Date start_date;
    private Date end_date;


    @Enumerated(EnumType.STRING)
    private TypeRoutines type_routine;

}