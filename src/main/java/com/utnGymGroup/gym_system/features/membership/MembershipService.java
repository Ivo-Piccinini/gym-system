package com.utnGymGroup.gym_system.features.membership;

import com.utnGymGroup.gym_system.features.audit.AuditActions;
import com.utnGymGroup.gym_system.features.audit.Auditable;
import com.utnGymGroup.gym_system.features.membership.dtos.MembershipRequestDTO;
import com.utnGymGroup.gym_system.features.membership.dtos.MembershipResponseDto;
import com.utnGymGroup.gym_system.features.membership.exceptions.MembershipAlreadyExistsException;
import com.utnGymGroup.gym_system.features.membership.exceptions.MembershipNotFoundException;
import com.utnGymGroup.gym_system.features.membership.mappers.MembershipRequestMapper;
import com.utnGymGroup.gym_system.features.membership.mappers.MembershipResponseMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MembershipService {

    private final MembershipRepository membershipRepository;
    private final MembershipRequestMapper requestMapper;
    private final MembershipResponseMapper responseMapper;

    public List<MembershipResponseDto> getAllMemberships() {
        return membershipRepository.findAll()
                .stream()
                .map(responseMapper::convertToDto)
                .toList();
    }

    public MembershipResponseDto getMembershipById(UUID id) {
        return membershipRepository.findByPublicId(id)
                .map(responseMapper::convertToDto)
                .orElseThrow(() -> new MembershipNotFoundException("No se encontró el plan de membresía con ID: " + id));
    }

    @Auditable(AuditActions.CREATE_PLAN)
    @Transactional
    public MembershipResponseDto createMembership(MembershipRequestDTO dto) {
        if (membershipRepository.existsByName(dto.getName())) {
            throw new MembershipAlreadyExistsException("Ya existe un plan con el nombre: " + dto.getName());
        }
        MembershipEntity entity = requestMapper.convertToEntity(dto);
        return responseMapper.convertToDto(membershipRepository.save(entity));
    }



}