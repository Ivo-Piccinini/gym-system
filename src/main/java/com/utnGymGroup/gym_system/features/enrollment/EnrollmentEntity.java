package com.utnGymGroup.gym_system.features.enrollment;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@ToString
@Entity
@Table(name = "enrollments")
public class EnrollmentEntity {
    private Long id;
    private
}
