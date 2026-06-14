package com.utnGymGroup.gym_system.features.routine;


import com.utnGymGroup.gym_system.features.user.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

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

    @Column(nullable = false,updatable = false)
    private UUID publicId;


    private String name;

    @Temporal(TemporalType.DATE)
    @Column(name = "start_date")
    private Date startDate;

    @Temporal(TemporalType.DATE)
    @Column(name = "end_date")
    private Date endDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private UserEntity client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name ="professor_id")
    private UserEntity professor;

    @Enumerated(EnumType.STRING)
    private TypeRoutines type_routine;

    @PrePersist
    private void prePersist() {
        if (this.publicId == null) {
            this.publicId = UUID.randomUUID();
        }
    }

}