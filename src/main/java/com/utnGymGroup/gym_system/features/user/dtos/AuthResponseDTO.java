package com.utnGymGroup.gym_system.features.user.dtos;


import lombok.*;

// lo que se devuelve cuando el login es exitoso

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class AuthResponseDTO {
    private String token;
    private String username;
    private String role; // para que el front sepa que mostrar
}
