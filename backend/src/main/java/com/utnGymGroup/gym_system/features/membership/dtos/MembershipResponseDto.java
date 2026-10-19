package com.utnGymGroup.gym_system.features.membership.dtos;
import lombok.*;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MembershipResponseDto {

    private Long id;
    private UUID publicId;
    private String name;
    private Double price;
    private Integer durationDays;
}
