package com.utnGymGroup.gym_system.features.memberships;

import com.utnGymGroup.gym_system.common.interfaces.ICreate;
import com.utnGymGroup.gym_system.features.memberships.dtos.MembershipsRequestDTO;
import com.utnGymGroup.gym_system.features.memberships.dtos.MembershipsResponseDto; // 🎯 Importación obligatoria del nuevo Response DTO
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
public class MembershipsController {

    private final MembershipsService membershipsService;


    @GetMapping
    public ResponseEntity<List<MembershipsResponseDto>> getAllMemberships() {
        return ResponseEntity.ok(membershipsService.getAllMemberships());
    }


    @GetMapping("/{id}")
    public ResponseEntity<MembershipsResponseDto> getMembershipById(@PathVariable Long id) {
        return ResponseEntity.ok(membershipsService.getMembershipById(id));
    }


    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<MembershipsResponseDto> createMembership(@Validated(ICreate.class) @RequestBody MembershipsRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(membershipsService.createMembership(dto));
    }
}