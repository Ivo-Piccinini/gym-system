package com.utnGymGroup.gym_system.features.user;

import com.utnGymGroup.gym_system.common.auth.permissions.Roles;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "users")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private UUID publicId;

    private String firstName;

    private String lastName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(unique = true, nullable = false)
    private String dni;

    private String phone;

    private LocalDate birthDate;

    @PrePersist
    void onCreate(){
        if(this.publicId == null)
            this.publicId = UUID.randomUUID();
    }
}
