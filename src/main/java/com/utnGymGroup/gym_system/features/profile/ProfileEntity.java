package com.utnGymGroup.gym_system.features.profile;

import com.utnGymGroup.gym_system.features.user.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
@Entity
@Table(name = "profiles")
public class ProfileEntity {
    @Id
    private Long id;

    @OneToOne
    @MapsId // MapsId sirve para que el id de perfil sea el mismo que el de usuario, osea, si el usuario tiene id 50 su perfil tambien
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Column(unique = true, nullable = false)
    private String dni;

    private String firstName;
    private String lastName;
    private String phone;
    private LocalDate birthDate;
}
