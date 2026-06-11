package com.utnGymGroup.gym_system.features.subscription;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionsController {

    private final SubscriptionsService subscriptionsService;


    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<SubscriptionsDTO>> getAllSubscriptions() {
        return ResponseEntity.ok(subscriptionsService.getAllSubscriptions());
    }


    @GetMapping("/my-subscriptions")
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    public ResponseEntity<List<SubscriptionsDTO>> getMySubscriptions() {
        return ResponseEntity.ok(subscriptionsService.getMySubscriptions());
    }


    @PostMapping("/{planId}")
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    public ResponseEntity<SubscriptionsDTO> subscribe(@PathVariable Long planId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(subscriptionsService.subscribe(planId));
    }

    
    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    public ResponseEntity<SubscriptionsDTO> cancelSubscription(@PathVariable Long id) {
        return ResponseEntity.ok(subscriptionsService.cancelSubscription(id));
    }


}
