package com.utnGymGroup.gym_system.features.subscription;

import com.utnGymGroup.gym_system.features.subscription.dtos.SubscriptionResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;


    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<SubscriptionResponseDto>> getAllSubscriptions() {
        return ResponseEntity.ok(subscriptionService.getAllSubscriptions());
    }


    @GetMapping("/my-subscriptions")
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    public ResponseEntity<List<SubscriptionResponseDto>> getMySubscriptions() {
        return ResponseEntity.ok(subscriptionService.getMySubscriptions());
    }


    @PostMapping("/{planId}")
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    public ResponseEntity<SubscriptionResponseDto> subscribe(@PathVariable Long planId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(subscriptionService.subscribe(planId));
    }


    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    public ResponseEntity<SubscriptionResponseDto> cancelSubscription(@PathVariable Long id) {
        return ResponseEntity.ok(subscriptionService.cancelSubscription(id));
    }


}