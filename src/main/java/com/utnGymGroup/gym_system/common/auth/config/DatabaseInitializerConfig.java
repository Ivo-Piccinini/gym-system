package com.utnGymGroup.gym_system.common.auth.config;

import com.utnGymGroup.gym_system.common.auth.credentials.CredentialsEntity;
import com.utnGymGroup.gym_system.common.auth.credentials.CredentialsRepository;
import com.utnGymGroup.gym_system.common.auth.permissions.*;
import com.utnGymGroup.gym_system.features.user.UserEntity;
import com.utnGymGroup.gym_system.features.user.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class DatabaseInitializerConfig {

    @Bean
    @Transactional
    public CommandLineRunner initDatabase(
            PermitRepository permitRepository,
            RoleRepository roleRepository,
            CredentialsRepository credentialsRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            // 1. Evitar duplicados si la base de datos ya contiene credenciales cargadas
            if (credentialsRepository.count() > 0) return;

            System.out.println(">> Detectadas credenciales vacías. Limpiando y re-sembrando base de datos de prueba del Gimnasio...");

            // Limpieza de datos huérfanos de semillas previas fallidas
            credentialsRepository.deleteAll();
            userRepository.deleteAll();
            roleRepository.deleteAll();
            permitRepository.deleteAll();

            System.out.println(">> Cargando datos de prueba limpios...");

            // 2. Crear y guardar TODOS los permisos del enum Permits de forma dinámica y elegante
            Map<Permits, PermitEntity> permitEntities = new HashMap<>();
            for (Permits permit : Permits.values()) {
                PermitEntity entity = permitRepository.save(
                        PermitEntity.builder().permit(permit).build()
                );
                permitEntities.put(permit, entity);
            }

            // 3. Crear y guardar Roles asignando los permisos correspondientes

            // --- ROLE_CLIENT (Socio del gimnasio) ---
            RoleEntity roleClient = new RoleEntity(Roles.ROLE_CLIENT);
            roleClient.getPermits().add(permitEntities.get(Permits.USER_READ));
            roleClient.getPermits().add(permitEntities.get(Permits.USER_UPDATE));
            roleClient.getPermits().add(permitEntities.get(Permits.ACTIVITY_READ));
            roleClient.getPermits().add(permitEntities.get(Permits.CLASS_READ));
            roleClient.getPermits().add(permitEntities.get(Permits.MEMBERSHIP_READ));
            roleClient.getPermits().add(permitEntities.get(Permits.EXERCISE_READ));
            roleClient.getPermits().add(permitEntities.get(Permits.ROUTINE_READ));
            roleClient.getPermits().add(permitEntities.get(Permits.ENROLLMENT_CREATE));
            roleClient.getPermits().add(permitEntities.get(Permits.ENROLLMENT_READ));
            roleClient.getPermits().add(permitEntities.get(Permits.ENROLLMENT_UPDATE));
            roleClient.getPermits().add(permitEntities.get(Permits.SUBSCRIPTION_CREATE));
            roleClient.getPermits().add(permitEntities.get(Permits.SUBSCRIPTION_READ));
            roleClient.getPermits().add(permitEntities.get(Permits.PAYMENT_CREATE));
            roleClient.getPermits().add(permitEntities.get(Permits.PAYMENT_READ));
            roleRepository.save(roleClient);

            // --- ROLE_PROFESSOR (Profesor/Entrenador) ---
            RoleEntity roleProfessor = new RoleEntity(Roles.ROLE_PROFESSOR);
            // Lecturas de consulta básica
            roleProfessor.getPermits().add(permitEntities.get(Permits.USER_READ));
            roleProfessor.getPermits().add(permitEntities.get(Permits.ACTIVITY_READ));
            roleProfessor.getPermits().add(permitEntities.get(Permits.CLASS_READ));
            roleProfessor.getPermits().add(permitEntities.get(Permits.ENROLLMENT_READ));
            // Ejercicios (CRUD completo)
            roleProfessor.getPermits().add(permitEntities.get(Permits.EXERCISE_CREATE));
            roleProfessor.getPermits().add(permitEntities.get(Permits.EXERCISE_READ));
            roleProfessor.getPermits().add(permitEntities.get(Permits.EXERCISE_UPDATE));
            roleProfessor.getPermits().add(permitEntities.get(Permits.EXERCISE_DELETE));
            // Rutinas (CRUD completo)
            roleProfessor.getPermits().add(permitEntities.get(Permits.ROUTINE_CREATE));
            roleProfessor.getPermits().add(permitEntities.get(Permits.ROUTINE_READ));
            roleProfessor.getPermits().add(permitEntities.get(Permits.ROUTINE_UPDATE));
            roleProfessor.getPermits().add(permitEntities.get(Permits.ROUTINE_DELETE));
            roleRepository.save(roleProfessor);

            // --- ROLE_ADMIN (Administrador general) ---
            RoleEntity roleAdmin = new RoleEntity(Roles.ROLE_ADMIN);
            // El administrador tiene TODOS los permisos del sistema
            roleAdmin.getPermits().addAll(permitEntities.values());
            roleRepository.save(roleAdmin);

            // 4. Crear credenciales y contraseñas de prueba encriptadas
            String passwordPlano = "password123";
            String passwordEncriptada = passwordEncoder.encode(passwordPlano);

            // --- Ejemplo: Crear Administrador ---
            UserEntity adminUser = UserEntity.builder()
                    .email("admin@gym.com")
                    .enabled(true)
                    .username("admin")
                    .password(passwordEncriptada)
                    .role(Roles.ROLE_ADMIN)
                    .build();
            userRepository.save(adminUser);

            CredentialsEntity adminCreds = CredentialsEntity.builder()
                    .username("admin")
                    .password(passwordEncriptada)
                    .enabled(true)
                    .user(adminUser)
                    .build();
            adminCreds.getRoles().add(roleAdmin);
            credentialsRepository.save(adminCreds);

            // --- Ejemplo: Crear 9 Clientes de prueba de forma dinámica ---
            for (int i = 1; i <= 9; i++) {
                UserEntity clientUser = UserEntity.builder()
                        .email("cliente" + i + "@gym.com")
                        .enabled(i != 9) // El cliente 9 se crea inactivo
                        .username("cliente" + i)
                        .password(passwordEncriptada)
                        .role(Roles.ROLE_CLIENT)
                        .build();
                userRepository.save(clientUser);

                CredentialsEntity clientCreds = CredentialsEntity.builder()
                        .username("cliente" + i)
                        .password(passwordEncriptada)
                        .enabled(i != 9)
                        .user(clientUser)
                        .build();
                clientCreds.getRoles().add(roleClient);
                credentialsRepository.save(clientCreds);
            }

            System.out.println(">> ¡Datos de prueba del gimnasio cargados exitosamente usando PasswordEncoder!");
        };
    }
}
