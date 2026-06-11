package com.utnGymGroup.gym_system.features.membership.mappers;

import com.utnGymGroup.gym_system.common.interfaces.IMapper;
import com.utnGymGroup.gym_system.features.membership.MembershipEntity;
import com.utnGymGroup.gym_system.features.membership.dtos.MembershipRequestDTO;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MembershipRequestMapper implements IMapper<MembershipEntity, MembershipRequestDTO> {
    private final ModelMapper modelMapper;

    @Override
    public MembershipRequestDTO convertToDto(MembershipEntity membershipEntity) {
        return modelMapper.map(membershipEntity, MembershipRequestDTO.class);
    }

    @Override
    public MembershipEntity convertToEntity(MembershipRequestDTO membershipRequestDTO) {
        return modelMapper.map(membershipRequestDTO, MembershipEntity.class);
    }

    @Override
    public void updateEntityFromDTO(MembershipRequestDTO membershipRequestDTO, MembershipEntity membershipEntity) {
        modelMapper.map(membershipRequestDTO, membershipEntity);
    }
}
