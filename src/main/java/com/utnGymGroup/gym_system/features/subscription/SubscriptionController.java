package com.utnGymGroup.gym_system.features.subscription;

import com.utnGymGroup.gym_system.features.subscription.dtos.SubscriptionResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/subscriptions")
@RequiredArgsConstructor
@Tag(name = "Suscripciones", description = "Controlador para la gestión de inscripciones de clientes a planes")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Obtener todas las suscripciones", description = "Devuelve el listado global de todas las suscripciones del sistema. Permitido únicamente para ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listado general obtenido correctamente"),
            @ApiResponse(responseCode = "403", description = "No tenés permisos para realizar esta acción")
    })
    public ResponseEntity<List<SubscriptionResponseDto>> getAllSubscriptions() {
        return ResponseEntity.ok(subscriptionService.getAllSubscriptions());
    }

    @GetMapping("/my-subscriptions")
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    @Operation(summary = "Obtener suscripciones del cliente autenticado", description = "Devuelve las suscripciones (activas, vencidas, etc.) del cliente logueado. Permitido para CLIENT.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Suscripciones del cliente obtenidas correctamente"),
            @ApiResponse(responseCode = "403", description = "No tenés permisos para realizar esta acción")
    })
    public ResponseEntity<List<SubscriptionResponseDto>> getMySubscriptions() {
        return ResponseEntity.ok(subscriptionService.getMySubscriptions());
    }

    @PostMapping("/{planId}")
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    @Operation(summary = "Suscribirse a un plan", description = "Crea una nueva suscripción activa vinculando al cliente autenticado con el plan indicado. Permitido para CLIENT.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Suscripción dada de alta exitosamente"),
            @ApiResponse(responseCode = "404", description = "No se encontró el plan especificado"),
            @ApiResponse(responseCode = "403", description = "No tenés permisos para realizar esta acción")
    })
    public ResponseEntity<SubscriptionResponseDto> subscribe(@PathVariable UUID planId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(subscriptionService.subscribe(planId));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    @Operation(summary = "Cancelar una suscripción", description = "Realiza la cancelación lógica de una suscripción vigente. Permitido para CLIENT (dueño de la suscripción).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Suscripción cancelada exitosamente"),
            @ApiResponse(responseCode = "400", description = "La suscripción ya se encuentra cancelada o expirada"),
            @ApiResponse(responseCode = "404", description = "No se encontró la suscripción con ese ID o no te pertenece")
    })
    public ResponseEntity<SubscriptionResponseDto> cancelSubscription(@PathVariable UUID id) {
        return ResponseEntity.ok(subscriptionService.cancelSubscription(id));
    }
}