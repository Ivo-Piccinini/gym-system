package com.utnGymGroup.gym_system.features.user;

import com.utnGymGroup.gym_system.common.auth.permissions.Roles;
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
import org.apache.catalina.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "Endpoints para la gestión integral de perfiles de usuario, credenciales y baja lógica.")
public class UserController {
    private final UserService userService;

    @GetMapping
    @Operation(
            summary = "Listar usuarios",
            description = "Recupera una lista con los perfiles e información de todos los usuarios registrados, con la opción de filtrar por estado activo o inactivo."
    )
    @ApiResponse(responseCode = "200", description = "Listado obtenido exitosamente.")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('PROFESSOR') and #role == null)")
    public ResponseEntity<List<UserDTO>> getUsers(
            @Parameter(description = "Filtrar por estado activo (true) o inactivo (false)", required = false)
            @RequestParam(required = false) Boolean enabled,

            @Parameter(description = "Filtrar por rol de usuario (ROLE_ADMIN, ROLE_PROFESSOR, ROLE_CLIENT)", required = false)
            @RequestParam(required = false) Roles role
        ){
        return ResponseEntity.ok(userService.findAllUsers(enabled, role));
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
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR') or #username == authentication.name")
    public ResponseEntity<UserDTO> getByUsername(
            @Parameter(description = "Nombre de usuario del gimnasio (ej. admin, cliente1)", required = true)
            @PathVariable String username
    ){
        return ResponseEntity.ok(userService.findByUsername(username));
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
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> register(
            @Validated(ICreate.class)
            @RequestBody UserDTO request
    ){
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.userRegister(request));
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
    @PreAuthorize("hasAnyRole('ADMIN') or #username == authentication.name")
    public ResponseEntity<UserDTO> updateProfile(
            @Parameter(description = "Nombre del usuario a modificar", required = true)
            @PathVariable String username,
            @Validated(IUpdate.class)
            @RequestBody UserDTO request
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
    @PreAuthorize("#username == authentication.name")
    public ResponseEntity<Void> changePassword(
            @Parameter(description = "Nombre del usuario que desea cambiar su contraseña", required = true)
            @PathVariable String username,
            @Validated(IUpdate.class)
            @RequestBody PasswordChangeDTO request
    ){
        userService.changePassword(username, request);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{username}/username")
    @PreAuthorize("#username == authentication.name") // Solo el propio usuario autenticado puede cambiarse su propio username
    @Operation(summary = "Personalizar el nombre de usuario por primera y única vez")
    public ResponseEntity<Void> changeUsername(
            @PathVariable String username,
            @Validated @RequestBody UsernameChangeDTO request
    ) {
        userService.changeUsername(username, request.newUsername());
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
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> toggleStatus(
            @Parameter(description = "Nombre del usuario a habilitar/deshabilitar", required = true)
            @PathVariable String username,
            @Parameter(description = "Nuevo estado de la cuenta (true = activo, false = inactivo)", required = true)
            @RequestParam boolean enabled
    ){
        userService.toggleUserStatus(username, enabled);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar físicamente un usuario y sus credenciales de la base de datos")
    public ResponseEntity<Void> deleteUser(@PathVariable String username){
        userService.deleteUser(username);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/me")
    @Operation(
            summary = "Actualización parcial del perfil del usuario autenticado",
            description = "Permite al usuario logueado actualizar de manera parcial sus datos personales (nombre, apellido, teléfono, fecha de nacimiento, etc.) sin alterar el resto de la información."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Perfil actualizado con éxito."),
            @ApiResponse(responseCode = "400", description = "Los datos provistos no son válidos."),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado.")
    })
    public ResponseEntity<UserDTO> updateLoggedUser(
            @Valid @RequestBody UserPatchDTO request
    ){
        return ResponseEntity.ok(userService.updatePartialUser(request));
    }

}
