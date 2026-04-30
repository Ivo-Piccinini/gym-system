package com.utnGymGroup.gym_system.features.enrollment;

import com.utnGymGroup.gym_system.features.classG.ClassEntity;
import com.utnGymGroup.gym_system.features.user.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@ToString
@Entity
@Table(name = "enrollments")
public class EnrollmentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "client_id",nullable = false)
    private UserEntity client;

    @JoinColumn(name = "class_id",nullable = false)
    private ClassEntity gymClass;

    @Column(name = "enrollment_date",nullable = false)
    private LocalDate enrollmentDate;

}
