package com.utnGymGroup.gym_system.features.subscription;

import com.utnGymGroup.gym_system.features.subscription.dtos.SubscriptionsResponseDto;
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
    public ResponseEntity<List<SubscriptionsResponseDto>> getAllSubscriptions() {
        return ResponseEntity.ok(subscriptionsService.getAllSubscriptions());
    }


    @GetMapping("/my-subscriptions")
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    public ResponseEntity<List<SubscriptionsResponseDto>> getMySubscriptions() {
        return ResponseEntity.ok(subscriptionsService.getMySubscriptions());
    }


    @PostMapping("/{planId}")
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    public ResponseEntity<SubscriptionsResponseDto> subscribe(@PathVariable Long planId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(subscriptionsService.subscribe(planId));
    }


    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    public ResponseEntity<SubscriptionsResponseDto> cancelSubscription(@PathVariable Long id) {
        return ResponseEntity.ok(subscriptionsService.cancelSubscription(id));
    }


}