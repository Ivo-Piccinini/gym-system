package com.utnGymGroup.gym_system.features.user.dtos;

import com.utnGymGroup.gym_system.features.profile.ProfileDTO;
import jakarta.validation.Valid;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserUpdateDTO {
    @Valid
    private ProfileDTO profile;
}
