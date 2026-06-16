package com.utnGymGroup.gym_system.features.user;

import com.utnGymGroup.gym_system.common.auth.permissions.Roles;
import com.utnGymGroup.gym_system.common.interfaces.ICreate;
import com.utnGymGroup.gym_system.common.interfaces.IUpdate;
import com.utnGymGroup.gym_system.features.user.dtos.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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
    @ApiResponse(responseCode = "200", description = "Listado obtenido exitosamente.",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserDTO.class))))
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<List<UserDTO>> getUsers(
            @Parameter(description = "Filtrar por estado activo (true) o inactivo (false)", required = false)
            @RequestParam(required = false) Boolean enabled,

            @Parameter(description = "Filtrar por rol de usuario (ROLE_ADMIN, ROLE_PROFESSOR, ROLE_CLIENT)", required = false)
            @RequestParam(required = false) Roles role
        ){
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        boolean isProfessor = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_PROFESSOR"));

        if (isProfessor && role != null && role != Roles.ROLE_CLIENT) {
            throw new org.springframework.security.access.AccessDeniedException("Los profesores solo pueden filtrar por rol de cliente (ROLE_CLIENT).");
        }
        return ResponseEntity.ok(userService.findAllUsers(enabled, role));
    }

    @GetMapping("/{username}")
    @Operation(
            summary = "Obtener usuario por username",
            description = "Busca y devuelve la información y perfil de un usuario específico a partir de su nombre de usuario único."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario encontrado y devuelto con éxito.",
                    content = @Content(schema = @Schema(implementation = UserDTO.class))),
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
            @ApiResponse(responseCode = "201", description = "Usuario y credenciales creados exitosamente.",
                    content = @Content(schema = @Schema(implementation = UserDTO.class))),
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
            @ApiResponse(responseCode = "200", description = "Perfil actualizado con éxito.",
                    content = @Content(schema = @Schema(implementation = UserDTO.class))),
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
    @Operation(
            summary = "Personalizar el nombre de usuario por primera y única vez",
            description = "Permite personalizar el nombre de usuario del socio por primera vez cuando ha sido registrado con su DNI por defecto."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Nombre de usuario cambiado con éxito."),
            @ApiResponse(responseCode = "400", description = "El nuevo nombre de usuario no es válido o ya se encuentra en uso.")
    })
    public ResponseEntity<Void> changeUsername(
            @PathVariable String username,
            @Validated @RequestBody UsernameChangeDTO request
    ) {
        userService.changeUsername(username, request.newUsername());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/status")
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
            @Parameter(description = "ID público del usuario (UUID)", required = true)
            @PathVariable("id") UUID id,
            @Parameter(description = "Nuevo estado de la cuenta (true = activo, false = inactivo)", required = true)
            @RequestParam boolean enabled
    ){
        userService.toggleUserStatus(id, enabled);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Eliminar físicamente un usuario",
            description = "Elimina permanentemente de la base de datos la cuenta y credenciales asociadas al nombre de usuario provisto."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Usuario eliminado correctamente."),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado.")
    })
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
            @ApiResponse(responseCode = "200", description = "Perfil actualizado con éxito.",
                    content = @Content(schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "400", description = "Los datos provistos no son válidos."),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado.")
    })
    public ResponseEntity<UserDTO> updateLoggedUser(
            @Valid @RequestBody UserPatchDTO request
    ){
        return ResponseEntity.ok(userService.updatePartialUser(request));
    }

}

