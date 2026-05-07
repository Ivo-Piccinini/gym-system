package com.utnGymGroup.gym_system.features.classG;

import com.utnGymGroup.gym_system.features.activity.ActivityEntity;
import com.utnGymGroup.gym_system.features.user.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "classes")

public class ClassEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,unique = true,updatable = false)
    private UUID externalId;

    @JoinColumn(name = "activity_id",nullable = false)
    private ActivityEntity activity;

    @JoinColumn(name = "professor_id",nullable = false)
    private UserEntity professor;

    @Column(name = "day_of_week", nullable = false)
    @Enumerated(EnumType.STRING)
    private DayOfWeek dayOfWeek;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "capacity_max", nullable = false)
    private int capacityMax;

    @PrePersist
    void onCreate() {
        if (this.externalId == null) {
            this.externalId = UUID.randomUUID();
        }
    }




}
