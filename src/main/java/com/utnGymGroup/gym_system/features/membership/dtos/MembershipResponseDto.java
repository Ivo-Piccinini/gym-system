package com.utnGymGroup.gym_system.features.membership.dtos;
import lombok.*;
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MembershipResponseDto {

    private Long id;
    private String name;
    private Double price;
    private Integer durationDays;
}
