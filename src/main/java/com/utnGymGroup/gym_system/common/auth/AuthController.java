package com.utnGymGroup.gym_system.common.auth;

import com.utnGymGroup.gym_system.common.auth.dto.AuthRequest;
import com.utnGymGroup.gym_system.common.auth.dto.AuthResponse;
import com.utnGymGroup.gym_system.common.auth.dto.NewAccountRequest;
import com.utnGymGroup.gym_system.common.auth.jwt.JwtService;
import com.utnGymGroup.gym_system.features.user.UserService;
import com.utnGymGroup.gym_system.features.user.dtos.UserResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Endpoints para registro público e inicio de sesión de usuarios.")
public class AuthController {
    private final AuthService authService;
    private final UserService userService;
    private final JwtService jwtService;

    @PostMapping("/login")
    @Operation(
            summary = "Iniciar sesión de usuario",
            description = "Autentica las credenciales de usuario provistas (nombre de usuario y contraseña) y retorna un token JWT válido para consumir endpoints seguros."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token JWT generado con éxito."),
            @ApiResponse(responseCode = "401", description = "Credenciales de inicio de sesión inválidas o cuenta de usuario inactiva.")
    })
    public ResponseEntity<AuthResponse> authenticateUser(@RequestBody AuthRequest authRequest){
        UserDetails user = authService.authenticate(authRequest);
        String token = jwtService.generateToken(user);
        return ResponseEntity.ok(new AuthResponse(token));
    }

    @PostMapping("/register")
    @Operation(
            summary = "Registro público de nuevo usuario cliente",
            description = "Crea una nueva cuenta de usuario básica con rol inicial de cliente (socio) sin necesidad de requerir detalles iniciales de perfil."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario registrado con éxito."),
            @ApiResponse(responseCode = "400", description = "El nombre de usuario o correo electrónico ya se encuentra registrado.")
    })
    public ResponseEntity<UserResponseDTO> registerUser(@RequestBody NewAccountRequest newAccountRequest){
        return new ResponseEntity<>(userService.userRegister(newAccountRequest), HttpStatus.CREATED);
    }
}
