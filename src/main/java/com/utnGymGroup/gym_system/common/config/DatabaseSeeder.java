package com.utnGymGroup.gym_system.common.config;

import com.utnGymGroup.gym_system.features.profile.ProfileEntity;
import com.utnGymGroup.gym_system.features.user.Roles;
import com.utnGymGroup.gym_system.features.user.UserEntity;
import com.utnGymGroup.gym_system.features.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DatabaseSeeder implements CommandLineRunner {
    private final UserRepository userRepository;

    public DatabaseSeeder(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) throws Exception{
        if(userRepository.count() == 0){
            ProfileEntity adminProfile = ProfileEntity.builder()
                    .dni("00000000")
                    .firstName("Administrador")
                    .lastName("Principal")
                    .phone("123456789")
                    .build();

            UserEntity adminUser = UserEntity.builder()
                    .username("admin")
                    .password("admin123") // TODO: cambiar cuando veamos spring security
                    .email("admin@admin.com")
                    .enabled(true)
                    .role(Roles.ADMIN)
                    .profile(adminProfile)
                    .build();

            adminProfile.setUser(adminUser);
            userRepository.save(adminUser);

            System.out.println("=========================================================================");
            System.out.println(">> BASE DE DATOS INICIALIZADA");
            System.out.println(">> Se ha creado el usuario Administrador inicial:");
            System.out.println(">> Username: admin");
            System.out.println(">> Password: admin123");
            System.out.println("=========================================================================");

        }
    }
}
