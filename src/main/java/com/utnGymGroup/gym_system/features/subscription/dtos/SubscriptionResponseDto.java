package com.utnGymGroup.gym_system.features.subscription.dtos;
import java.time.LocalDate;
import com.utnGymGroup.gym_system.features.subscription.SubscriptionStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SubscriptionResponseDto {

    private Long id;
    private Long userId;
    private Long planId;
    private LocalDate startDate;
    private LocalDate endDate;
    private SubscriptionStatus status;


}
