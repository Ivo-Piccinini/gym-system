package com.utnGymGroup.gym_system.features.membership.mappers;

import com.utnGymGroup.gym_system.common.interfaces.IMapper;
import com.utnGymGroup.gym_system.features.membership.MembershipEntity;
import com.utnGymGroup.gym_system.features.membership.dtos.MembershipResponseDto;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MembershipResponseMapper implements IMapper<MembershipEntity, MembershipResponseDto> {

    private final ModelMapper modelMapper;

    @Override
    public MembershipResponseDto convertToDto(MembershipEntity membershipEntity) {
        return modelMapper.map(membershipEntity, MembershipResponseDto.class);
    }

    @Override
    public MembershipEntity convertToEntity(MembershipResponseDto membershipResponseDto) {
        return modelMapper.map(membershipResponseDto, MembershipEntity.class);
    }

    @Override
    public void updateEntityFromDTO(MembershipResponseDto membershipResponseDto, MembershipEntity membershipEntity) {
        modelMapper.map(membershipResponseDto, membershipEntity);
    }
}