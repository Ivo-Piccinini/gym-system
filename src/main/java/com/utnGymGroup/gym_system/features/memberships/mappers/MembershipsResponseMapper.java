package com.utnGymGroup.gym_system.features.memberships.mappers;

import com.utnGymGroup.gym_system.common.interfaces.IMapper;
import com.utnGymGroup.gym_system.features.memberships.MembershipsEntity;
import com.utnGymGroup.gym_system.features.memberships.dtos.MembershipsResponseDto;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MembershipsResponseMapper implements IMapper<MembershipsEntity, MembershipsResponseDto> {

    private final ModelMapper modelMapper;

    @Override
    public MembershipsResponseDto convertToDto(MembershipsEntity membershipsEntity) {
        return modelMapper.map(membershipsEntity, MembershipsResponseDto.class);
    }

    @Override
    public MembershipsEntity convertToEntity(MembershipsResponseDto membershipsResponseDto) {
        return modelMapper.map(membershipsResponseDto, MembershipsEntity.class);
    }

    @Override
    public void updateEntityFromDTO(MembershipsResponseDto membershipsResponseDto, MembershipsEntity membershipsEntity) {
        modelMapper.map(membershipsResponseDto, membershipsEntity);
    }
}