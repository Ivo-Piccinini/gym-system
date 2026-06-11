package com.utnGymGroup.gym_system.features.memberships;

import com.utnGymGroup.gym_system.features.memberships.dtos.MembershipsRequestDTO;
import com.utnGymGroup.gym_system.features.memberships.dtos.MembershipsResponseDto;
import com.utnGymGroup.gym_system.features.memberships.exceptions.MembershipAlreadyExistsException;
import com.utnGymGroup.gym_system.features.memberships.exceptions.MembershipNotFoundException;
import com.utnGymGroup.gym_system.features.memberships.mappers.MembershipRequestMapper;
import com.utnGymGroup.gym_system.features.memberships.mappers.MembershipsResponseMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MembershipsService {

    private final MembershipsRepository membershipsRepository;
    private final MembershipRequestMapper requestMapper;
    private final MembershipsResponseMapper responseMapper;

    public List<MembershipsResponseDto> getAllMemberships() {
        return membershipsRepository.findAll()
                .stream()
                .map(responseMapper::convertToDto)
                .toList();
    }

    public MembershipsResponseDto getMembershipById(Long id) {
        return membershipsRepository.findById(id)
                .map(responseMapper::convertToDto)
                .orElseThrow(() -> new MembershipNotFoundException("No se encontró el plan de membresía con ID: " + id));
    }

    @Transactional
    public MembershipsResponseDto createMembership(MembershipsRequestDTO dto) {
        if (membershipsRepository.existsByName(dto.getName())) {
            throw new MembershipAlreadyExistsException("Ya existe un plan con el nombre: " + dto.getName());
        }
        MembershipsEntity entity = requestMapper.convertToEntity(dto);
        return responseMapper.convertToDto(membershipsRepository.save(entity));
    }
}