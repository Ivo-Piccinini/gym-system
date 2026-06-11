package com.utnGymGroup.gym_system.features.memberships.dtos;
import lombok.*;
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MembershipsResponseDto {

    private Long id;
    private String name;
    private Double price;
    private Integer durationDays;
}
