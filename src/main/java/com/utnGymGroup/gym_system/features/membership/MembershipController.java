package com.utnGymGroup.gym_system.features.membership;

import com.utnGymGroup.gym_system.common.interfaces.ICreate;
import com.utnGymGroup.gym_system.features.membership.dtos.MembershipRequestDTO;
import com.utnGymGroup.gym_system.features.membership.dtos.MembershipResponseDto; // 🎯 Importación obligatoria del nuevo Response DTO
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/memberships")
@RequiredArgsConstructor
public class MembershipController {

    private final MembershipService membershipService;


    @GetMapping
    public ResponseEntity<List<MembershipResponseDto>> getAllMemberships() {
        return ResponseEntity.ok(membershipService.getAllMemberships());
    }


    @GetMapping("/{id}")
    public ResponseEntity<MembershipResponseDto> getMembershipById(@PathVariable Long id) {
        return ResponseEntity.ok(membershipService.getMembershipById(id));
    }


    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<MembershipResponseDto> createMembership(@Validated(ICreate.class) @RequestBody MembershipRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(membershipService.createMembership(dto));
    }
}