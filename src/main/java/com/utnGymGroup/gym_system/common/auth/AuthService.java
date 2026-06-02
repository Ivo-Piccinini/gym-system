package com.utnGymGroup.gym_system.common.auth;

import com.utnGymGroup.gym_system.common.auth.credentials.CredentialsEntity;
import com.utnGymGroup.gym_system.common.auth.credentials.CredentialsRepository;
import com.utnGymGroup.gym_system.common.auth.dto.AuthRequest;
import com.utnGymGroup.gym_system.common.auth.dto.AuthResponse;
import com.utnGymGroup.gym_system.common.auth.jwt.JwtService;
import com.utnGymGroup.gym_system.features.user.exceptions.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final CredentialsRepository credentialsRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthResponse login(AuthRequest input){
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        input.username(),
                        input.password()
                )
        );

        CredentialsEntity credentials = credentialsRepository.findByUsername(input.username())
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado. USERNAME: " + input.username()));

        String token = jwtService.generateToken(credentials);

        boolean isFirstLogin = credentials.getUsername().equals(credentials.getUser().getDni());

        String message = isFirstLogin
                ? "Por seguridad, debes cambiar tu nombre de usuario y contraseña en tu primer ingreso."
                : "Inicio de sesión exitoso.";
        // 5. Devolver el DTO final armado
        return new AuthResponse(token, message);
    }
}
