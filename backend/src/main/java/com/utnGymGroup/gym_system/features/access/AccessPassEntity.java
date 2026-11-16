package com.utnGymGroup.gym_system.features.access;

import com.utnGymGroup.gym_system.features.subscription.SubscriptionEntity;
import com.utnGymGroup.gym_system.features.user.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "access_passes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccessPassEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID publicId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "subscription_id", nullable = false)
    private SubscriptionEntity subscription;

    @Column(nullable = false, unique = true)
    private String accessCode;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String qrData;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String qrImageBase64;

    @Column(nullable = false)
    private String generatedBy;

    @Column(nullable = false)
    private LocalDateTime generatedAt;

    @Column(nullable = false)
    private LocalDate validUntil;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @PrePersist
    void onCreate() {
        if (this.publicId == null) {
            this.publicId = UUID.randomUUID();
        }
        if (this.generatedAt == null) {
            this.generatedAt = LocalDateTime.now();
        }
    }
}
