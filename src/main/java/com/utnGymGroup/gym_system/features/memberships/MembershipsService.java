package com.utnGymGroup.gym_system.features.memberships;


import com.utnGymGroup.gym_system.features.memberships.exceptions.MembershipNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MembershipsService {

    private final MembershipsMapper membershipsMapper;
    private final MembershipsRepository membershipsRepository;

    public List<MembershipsDTO> getAllMemberships() {
        return membershipsRepository.findAll()
                .stream()
                .map(membershipsMapper::convertToDto)
                .toList();
    }

    public MembershipsDTO getMembershipById(Long id) {
        return membershipsRepository.findById(id)
                .map(membershipsMapper::convertToDto)
                .orElseThrow(() -> new MembershipNotFoundException("No se encontró el plan de membresía con ID: " + id));
    }

    @Transactional
    public MembershipsDTO createMembership(MembershipsDTO dto) {
        MembershipsEntity entity = membershipsMapper.convertToEntity(dto);
        return membershipsMapper.convertToDto(membershipsRepository.save(entity));
    }


}
