package com.utnGymGroup.gym_system.features.memberships.mappers;

import com.utnGymGroup.gym_system.common.interfaces.IMapper;
import com.utnGymGroup.gym_system.features.memberships.MembershipsEntity;
import com.utnGymGroup.gym_system.features.memberships.dtos.MembershipsRequestDTO;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MembershipRequestMapper implements IMapper<MembershipsEntity, MembershipsRequestDTO> {
    private final ModelMapper modelMapper;

    @Override
    public MembershipsRequestDTO convertToDto(MembershipsEntity membershipsEntity) {
        return modelMapper.map(membershipsEntity, MembershipsRequestDTO.class);
    }

    @Override
    public MembershipsEntity convertToEntity(MembershipsRequestDTO membershipsRequestDTO) {
        return modelMapper.map(membershipsRequestDTO, MembershipsEntity.class);
    }

    @Override
    public void updateEntityFromDTO(MembershipsRequestDTO membershipsRequestDTO, MembershipsEntity membershipsEntity) {
        modelMapper.map(membershipsRequestDTO, membershipsEntity);
    }
}
