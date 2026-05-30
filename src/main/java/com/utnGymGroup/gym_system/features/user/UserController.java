package com.utnGymGroup.gym_system.features.user;

import com.utnGymGroup.gym_system.common.interfaces.ICreate;
import com.utnGymGroup.gym_system.common.interfaces.IUpdate;
import com.utnGymGroup.gym_system.features.user.dtos.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "Endpoints para la gestión integral de perfiles de usuario, credenciales y baja lógica.")
public class UserController {
    private final UserService userService;

    @GetMapping
    @Operation(
            summary = "Listar todos los usuarios",
            description = "Recupera una lista con los perfiles e información de todos los usuarios registrados en el sistema del gimnasio."
    )
    @ApiResponse(responseCode = "200", description = "Listado obtenido exitosamente.")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers(){
        return ResponseEntity.ok(userService.findAllUsers());
    }

    @GetMapping("/{username}")
    @Operation(
            summary = "Obtener usuario por username",
            description = "Busca y devuelve la información y perfil de un usuario específico a partir de su nombre de usuario único."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario encontrado y devuelto con éxito."),
            @ApiResponse(responseCode = "404", description = "No se encontró ningún usuario con el username provisto.")
    })
    public ResponseEntity<UserResponseDTO> getByUsername(
            @Parameter(description = "Nombre de usuario del gimnasio (ej. admin, cliente1)", required = true)
            @PathVariable String username
    ){
        return ResponseEntity.ok(userService.findByUsername(username));
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getByStatus(@RequestParam Boolean enabled){
        return ResponseEntity.ok(userService.findAllUsersByStatus(enabled));
    }

    @PostMapping("/register")
    @Operation(
            summary = "Registro administrativo de nuevo usuario con perfil",
            description = "Registra un nuevo usuario en la base de datos validando los datos requeridos de su perfil personal (DNI, teléfono, fecha de nacimiento)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario y credenciales creados exitosamente."),
            @ApiResponse(responseCode = "400", description = "Los datos del usuario o perfil no cumplen con las reglas de validación.")
    })
    public ResponseEntity<UserResponseDTO> register(
            @Validated(ICreate.class)
            @RequestBody UserCreateRequestDTO request
    ){
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.userRegister(request));
    }

    @PostMapping("/login")
    @Operation(
            summary = "Iniciar sesión en panel de usuario",
            description = "Autentica al usuario en el sistema a partir de sus credenciales y provee un token JWT firmado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Autenticación exitosa. Devuelve datos del usuario y token."),
            @ApiResponse(responseCode = "401", description = "Credenciales incorrectas o cuenta inactiva.")
    })
    public ResponseEntity<AuthResponseDTO> login(
            @Valid
            @RequestBody LoginRequestDTO request
    ){
        return ResponseEntity.ok(userService.login(request));
    }

    @PutMapping("/{username}")
    @Operation(
            summary = "Actualizar perfil de usuario",
            description = "Actualiza los datos personales (teléfono, nombre, apellido, DNI, fecha de nacimiento) en el perfil de un usuario existente."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Perfil actualizado con éxito."),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado.")
    })
    public ResponseEntity<UserResponseDTO> updateProfile(
            @Parameter(description = "Nombre del usuario a modificar", required = true)
            @PathVariable String username,
            @Validated(IUpdate.class)
            @RequestBody UserUpdateDTO request
    ){
        return ResponseEntity.ok(userService.updateUser(username, request));
    }

    @PatchMapping("/{username}/password")
    @Operation(
            summary = "Cambiar contraseña de usuario",
            description = "Actualiza de manera segura la contraseña de un usuario validando de forma cifrada su clave actual y codificando la nueva contraseña."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contraseña cambiada con éxito."),
            @ApiResponse(responseCode = "400", description = "Las contraseñas no coinciden, la clave actual es incorrecta o la nueva contraseña es igual a la anterior.")
    })
    public ResponseEntity<Void> changePassword(
            @Parameter(description = "Nombre del usuario que desea cambiar su contraseña", required = true)
            @PathVariable String username,
            @Validated(IUpdate.class)
            @RequestBody PasswordChangeDTO request
    ){
        userService.changePassword(username, request);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{username}/status")
    @Operation(
            summary = "Baja lógica o reactivación de usuario",
            description = "Activa o desactiva la cuenta de un usuario y de sus credenciales de seguridad de manera coordinada."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado de cuenta actualizado correctamente."),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado.")
    })
    public ResponseEntity<Void> toggleStatus(
            @Parameter(description = "Nombre del usuario a habilitar/deshabilitar", required = true)
            @PathVariable String username,
            @Parameter(description = "Nuevo estado de la cuenta (true = activo, false = inactivo)", required = true)
            @RequestParam boolean enabled
    ){
        userService.toggleUserStatus(username, enabled);
        return ResponseEntity.ok().build();
    }

}
