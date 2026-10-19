package com.utnGymGroup.gym_system.features.membership;

import com.utnGymGroup.gym_system.common.interfaces.ICreate;
import com.utnGymGroup.gym_system.features.membership.dtos.MembershipRequestDTO;
import com.utnGymGroup.gym_system.features.membership.dtos.MembershipResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/memberships")
@RequiredArgsConstructor
@Tag(name = "Membresías", description = "Controlador para la gestión de planes de membresía del gimnasio")
public class MembershipController {

    private final MembershipService membershipService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or hasRole('CLIENT')")
    @Operation(summary = "Obtener todas las membresías", description = "Devuelve un listado con todos los planes de membresía disponibles. Permitido para ADMIN, PROFESSOR y CLIENT.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente"),
            @ApiResponse(responseCode = "403", description = "No tenés permisos para realizar esta acción")
    })
    public ResponseEntity<List<MembershipResponseDto>> getAllMemberships() {
        return ResponseEntity.ok(membershipService.getAllMemberships());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener membresía por ID", description = "Busca y devuelve los detalles de un plan de membresía específico mediante su UUID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Membresía encontrada correctamente"),
            @ApiResponse(responseCode = "404", description = "No se encontró el plan de membresía con el ID especificado")
    })
    public ResponseEntity<MembershipResponseDto> getMembershipById(@PathVariable UUID id) {
        return ResponseEntity.ok(membershipService.getMembershipById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear una nueva membresía", description = "Registra un nuevo plan de membresía en el sistema. Permitido únicamente para ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Membresía creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o el nombre ya existe"),
            @ApiResponse(responseCode = "403", description = "No tenés permisos para realizar esta acción")
    })
    public ResponseEntity<MembershipResponseDto> createMembership(@Validated(ICreate.class) @RequestBody MembershipRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(membershipService.createMembership(dto));
    }
}