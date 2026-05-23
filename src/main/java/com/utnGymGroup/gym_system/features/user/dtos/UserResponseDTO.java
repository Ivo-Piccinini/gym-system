package com.utnGymGroup.gym_system.features.user.dtos;

import com.utnGymGroup.gym_system.features.profile.ProfileDTO;
import com.utnGymGroup.gym_system.features.user.Roles;
import lombok.*;

// Este DTO es para enviar datos al frontend

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserResponseDTO {
    private String username;
    private String email;
    private Boolean enabled;
    private String role;
    private ProfileDTO profile; // objeto anidado para mostrar la información personal
}
