package com.utnGymGroup.gym_system.features.payments;

import com.utnGymGroup.gym_system.common.interfaces.ICreate;
import com.utnGymGroup.gym_system.features.payments.dtos.PaymentsRequestDTO;
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
public class PaymentsController {

    private final PaymentsService paymentsService;

    
    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<PaymentsRequestDTO>> getAllPayments() {
        return ResponseEntity.ok(paymentsService.getAllPayments());
    }


    @GetMapping("/my-payments")
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    public ResponseEntity<List<PaymentsRequestDTO>> getMyPayments() {
        return ResponseEntity.ok(paymentsService.getMyPayments());
    }


    @PostMapping
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    public ResponseEntity<PaymentsRequestDTO> createPayment(@Validated(ICreate.class) @RequestBody PaymentsRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentsService.createPayment(dto));
    }

}