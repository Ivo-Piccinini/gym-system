package com.utnGymGroup.gym_system.features.payment;

import com.utnGymGroup.gym_system.common.interfaces.ICreate;
import com.utnGymGroup.gym_system.features.payment.dtos.PaymentRequestDTO;
import com.utnGymGroup.gym_system.features.payment.dtos.PaymentResponseDto;
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

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Pagos", description = "Controlador para el registro y consulta de transacciones de pago")
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Obtener todos los pagos registrados", description = "Devuelve el historial global de pagos de todo el gimnasio. Permitido únicamente para ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Historial obtenido correctamente"),
            @ApiResponse(responseCode = "403", description = "No tenés permisos para realizar esta acción")
    })
    public ResponseEntity<List<PaymentResponseDto>> getAllPayments() {
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    @GetMapping("/my-payments")
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    @Operation(summary = "Obtener pagos del cliente autenticado", description = "Devuelve el historial de pagos pertenecientes al cliente que realiza la petición. Permitido para CLIENT.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Historial del cliente obtenido correctamente"),
            @ApiResponse(responseCode = "403", description = "No tenés permisos para realizar esta acción")
    })
    public ResponseEntity<List<PaymentResponseDto>> getMyPayments() {
        return ResponseEntity.ok(paymentService.getMyPayments());
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    @Operation(summary = "Registrar un nuevo pago", description = "Registra un pago asociado a una suscripción activa del cliente. Permitido para CLIENT.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Pago registrado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o error en la suscripción"),
            @ApiResponse(responseCode = "403", description = "No tenés permisos para realizar esta acción")
    })
    public ResponseEntity<PaymentResponseDto> createPayment(@Validated(ICreate.class) @RequestBody PaymentRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.createPayment(dto));
    }
}