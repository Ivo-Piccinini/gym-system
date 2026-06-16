package com.utnGymGroup.gym_system.common.auth.config;

import com.utnGymGroup.gym_system.common.auth.credentials.CredentialsEntity;
import com.utnGymGroup.gym_system.common.auth.credentials.CredentialsRepository;
import com.utnGymGroup.gym_system.common.auth.permissions.*;
import com.utnGymGroup.gym_system.features.user.UserEntity;
import com.utnGymGroup.gym_system.features.user.UserRepository;
import com.utnGymGroup.gym_system.features.membership.MembershipEntity;
import com.utnGymGroup.gym_system.features.membership.MembershipRepository;
import com.utnGymGroup.gym_system.features.activity.ActivityEntity;
import com.utnGymGroup.gym_system.features.activity.ActivityRepository;
import com.utnGymGroup.gym_system.features.exercise.ExerciseEntity;
import com.utnGymGroup.gym_system.features.exercise.ExerciseRepository;
import com.utnGymGroup.gym_system.features.exercise.MuscleGroup;
import com.utnGymGroup.gym_system.features.GymClass.GymClassEntity;
import com.utnGymGroup.gym_system.features.GymClass.GymClassRepository;
import com.utnGymGroup.gym_system.features.GymClass.DayOfWeek;
import jakarta.transaction.Transactional;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Configuration
public class DatabaseInitializerConfig {

    @Bean
    @Transactional
    public CommandLineRunner initDatabase(
            PermitRepository permitRepository,
            RoleRepository roleRepository,
            CredentialsRepository credentialsRepository,
            UserRepository userRepository,
            MembershipRepository membershipRepository,
            ActivityRepository activityRepository,
            ExerciseRepository exerciseRepository,
            GymClassRepository gymClassRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            System.out.println(">> Iniciando verificación de semillas de base de datos...");

            // 1. Sembrado de Permisos y Roles (Esencial para la seguridad)
            Map<Permits, PermitEntity> permitEntities = new HashMap<>();
            if (permitRepository.count() == 0) {
                System.out.println(">> Sembrando Permisos de seguridad...");
                for (Permits permit : Permits.values()) {
                    PermitEntity entity = permitRepository.save(
                            PermitEntity.builder().permit(permit).build()
                    );
                    permitEntities.put(permit, entity);
                }
            } else {
                for (PermitEntity entity : permitRepository.findAll()) {
                    permitEntities.put(entity.getPermit(), entity);
                }
            }

            RoleEntity roleClient;
            RoleEntity roleProfessor;
            RoleEntity roleAdmin;

            if (roleRepository.count() == 0) {
                System.out.println(">> Sembrando Roles de seguridad...");
                // --- ROLE_CLIENT (Socio del gimnasio) ---
                roleClient = new RoleEntity(Roles.ROLE_CLIENT);
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
                roleProfessor = new RoleEntity(Roles.ROLE_PROFESSOR);
                roleProfessor.getPermits().add(permitEntities.get(Permits.USER_READ));
                roleProfessor.getPermits().add(permitEntities.get(Permits.ACTIVITY_READ));
                roleProfessor.getPermits().add(permitEntities.get(Permits.CLASS_READ));
                roleProfessor.getPermits().add(permitEntities.get(Permits.ENROLLMENT_READ));
                roleProfessor.getPermits().add(permitEntities.get(Permits.EXERCISE_CREATE));
                roleProfessor.getPermits().add(permitEntities.get(Permits.EXERCISE_READ));
                roleProfessor.getPermits().add(permitEntities.get(Permits.EXERCISE_UPDATE));
                roleProfessor.getPermits().add(permitEntities.get(Permits.EXERCISE_DELETE));
                roleProfessor.getPermits().add(permitEntities.get(Permits.ROUTINE_CREATE));
                roleProfessor.getPermits().add(permitEntities.get(Permits.ROUTINE_READ));
                roleProfessor.getPermits().add(permitEntities.get(Permits.ROUTINE_UPDATE));
                roleProfessor.getPermits().add(permitEntities.get(Permits.ROUTINE_DELETE));
                roleRepository.save(roleProfessor);

                // --- ROLE_ADMIN (Administrador general) ---
                roleAdmin = new RoleEntity(Roles.ROLE_ADMIN);
                roleAdmin.getPermits().addAll(permitEntities.values());
                roleRepository.save(roleAdmin);
            } else {
                roleClient = roleRepository.findByRole(Roles.ROLE_CLIENT).orElseThrow();
                roleProfessor = roleRepository.findByRole(Roles.ROLE_PROFESSOR).orElseThrow();
                roleAdmin = roleRepository.findByRole(Roles.ROLE_ADMIN).orElseThrow();
            }

            // 2. Sembrado de Usuarios y Credenciales
            if (credentialsRepository.count() == 0) {
                System.out.println(">> Sembrando Usuarios y Credenciales de prueba...");
                String passwordPlano = "password123";
                String passwordEncriptada = passwordEncoder.encode(passwordPlano);

                // --- Administrador ---
                UserEntity adminUser = UserEntity.builder()
                        .firstName("admin")
                        .lastName("gym")
                        .email("admin@gym.com")
                        .dni("00000000")
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

                // --- Profesor ---
                UserEntity professorUser = UserEntity.builder()
                        .firstName("Profesor")
                        .lastName("Uno")
                        .email("profe@gym.com")
                        .dni("22222222")
                        .build();
                userRepository.save(professorUser);

                CredentialsEntity professorCreds = CredentialsEntity.builder()
                        .username("profesor")
                        .password(passwordEncriptada)
                        .enabled(true)
                        .user(professorUser)
                        .build();
                professorCreds.getRoles().add(roleProfessor);
                credentialsRepository.save(professorCreds);

                // --- 9 Clientes de prueba ---
                for (int i = 1; i <= 9; i++) {
                    UserEntity clientUser = UserEntity.builder()
                            .firstName("Cliente")
                            .lastName(String.valueOf(i))
                            .email("cliente" + i + "@gym.com")
                            .dni(String.format("1000000%d", i))
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
            }

            // 3. Sembrado de Planes Comerciales (Membresías)
            if (membershipRepository.count() == 0) {
                System.out.println(">> Sembrando Planes Comerciales...");
                MembershipEntity plan1 = new MembershipEntity();
                plan1.setName("Pase Libre Mensual");
                plan1.setPrice(15000.0);
                plan1.setDurationDays(30);
                membershipRepository.save(plan1);

                MembershipEntity plan2 = new MembershipEntity();
                plan2.setName("Pase Semestral");
                plan2.setPrice(72000.0);
                plan2.setDurationDays(180);
                membershipRepository.save(plan2);

                MembershipEntity plan3 = new MembershipEntity();
                plan3.setName("Pase Anual");
                plan3.setPrice(120000.0);
                plan3.setDurationDays(365);
                membershipRepository.save(plan3);
            }

            // 4. Sembrado de Actividades
            if (activityRepository.count() == 0) {
                System.out.println(">> Sembrando Actividades...");
                ActivityEntity act1 = new ActivityEntity();
                act1.setName("Musculación");
                act1.setDescription("Entrenamiento de fuerza y acondicionamiento muscular en sala de musculación.");
                act1.setActive(true);
                activityRepository.save(act1);

                ActivityEntity act2 = new ActivityEntity();
                act2.setName("Spinning");
                act2.setDescription("Clase grupal de ciclismo indoor de alta intensidad cardiovascular.");
                act2.setActive(true);
                activityRepository.save(act2);

                ActivityEntity act3 = new ActivityEntity();
                act3.setName("Crossfit");
                act3.setDescription("Entrenamiento funcional de alta intensidad que combina fuerza, resistencia y gimnasia.");
                act3.setActive(true);
                activityRepository.save(act3);
            }

            // 5. Sembrado de Ejercicios
            if (exerciseRepository.count() == 0) {
                System.out.println(">> Sembrando Ejercicios...");
                ExerciseEntity ex1 = ExerciseEntity.builder()
                        .idPublic(UUID.randomUUID())
                        .name("Press de Banca Plano")
                        .descripcion("Ejercicio clásico de empuje para desarrollar la fuerza y masa del pectoral.")
                        .muscleGroup(MuscleGroup.PECHO)
                        .enabled(true)
                        .build();
                exerciseRepository.save(ex1);

                ExerciseEntity ex2 = ExerciseEntity.builder()
                        .idPublic(UUID.randomUUID())
                        .name("Sentadillas Libres")
                        .descripcion("Ejercicio multiarticular enfocado en el desarrollo de los cuádriceps, glúteos e isquiotibiales.")
                        .muscleGroup(MuscleGroup.CUADRICEPS)
                        .enabled(true)
                        .build();
                exerciseRepository.save(ex2);

                ExerciseEntity ex3 = ExerciseEntity.builder()
                        .idPublic(UUID.randomUUID())
                        .name("Peso Muerto")
                        .descripcion("Ejercicio fundamental para toda la cadena posterior, glúteos e isquiotibiales.")
                        .muscleGroup(MuscleGroup.ISQUIOTIBIALES)
                        .enabled(true)
                        .build();
                exerciseRepository.save(ex3);

                ExerciseEntity ex4 = ExerciseEntity.builder()
                        .idPublic(UUID.randomUUID())
                        .name("Dominadas Pronas")
                        .descripcion("Ejercicio de tracción vertical para ensanchar y fortalecer la espalda.")
                        .muscleGroup(MuscleGroup.ESPALDA)
                        .enabled(true)
                        .build();
                exerciseRepository.save(ex4);

                ExerciseEntity ex5 = ExerciseEntity.builder()
                        .idPublic(UUID.randomUUID())
                        .name("Press Militar con Barra")
                        .descripcion("Ejercicio de empuje vertical para el desarrollo de los deltoides (hombros).")
                        .muscleGroup(MuscleGroup.HOMBROS)
                        .enabled(true)
                        .build();
                exerciseRepository.save(ex5);

                ExerciseEntity ex6 = ExerciseEntity.builder()
                        .idPublic(UUID.randomUUID())
                        .name("Curl de Bíceps con Barra")
                        .descripcion("Ejercicio de aislamiento clásico para el bíceps braquial.")
                        .muscleGroup(MuscleGroup.BICEPS)
                        .enabled(true)
                        .build();
                exerciseRepository.save(ex6);

                ExerciseEntity ex7 = ExerciseEntity.builder()
                        .idPublic(UUID.randomUUID())
                        .name("Extensión de Tríceps en Polea")
                        .descripcion("Ejercicio de aislamiento para fortalecer y dar volumen a los tríceps.")
                        .muscleGroup(MuscleGroup.TRICEPS)
                        .enabled(true)
                        .build();
                exerciseRepository.save(ex7);

                ExerciseEntity ex8 = ExerciseEntity.builder()
                        .idPublic(UUID.randomUUID())
                        .name("Hip Thrust")
                        .descripcion("El mejor ejercicio de aislamiento para el desarrollo de los glúteos.")
                        .muscleGroup(MuscleGroup.GLUTEOS)
                        .enabled(true)
                        .build();
                exerciseRepository.save(ex8);

                ExerciseEntity ex9 = ExerciseEntity.builder()
                        .idPublic(UUID.randomUUID())
                        .name("Elevaciones de Talones")
                        .descripcion("Ejercicio específico para el desarrollo de los gemelos.")
                        .muscleGroup(MuscleGroup.GEMELOS)
                        .enabled(true)
                        .build();
                exerciseRepository.save(ex9);

                ExerciseEntity ex10 = ExerciseEntity.builder()
                        .idPublic(UUID.randomUUID())
                        .name("Crunch Abdominal")
                        .descripcion("Ejercicio básico para fortalecer la pared abdominal.")
                        .muscleGroup(MuscleGroup.ABDOMINALES)
                        .enabled(true)
                        .build();
                exerciseRepository.save(ex10);
            }

            // 6. Sembrado de Clases (GymClassEntity)
            if (gymClassRepository.count() == 0) {
                System.out.println(">> Sembrando Clases...");

                // Obtener o crear al profesor
                UserEntity professor = userRepository.findByEmail("profe@gym.com").orElseGet(() -> {
                    System.out.println(">> Profesor no encontrado en migración parcial, creándolo...");
                    UserEntity newProfe = UserEntity.builder()
                            .firstName("Profesor")
                            .lastName("Uno")
                            .email("profe@gym.com")
                            .dni("22222222")
                            .build();
                    userRepository.save(newProfe);

                    String passwordPlano = "password123";
                    String passwordEncriptada = passwordEncoder.encode(passwordPlano);
                    RoleEntity roleProf = roleRepository.findByRole(Roles.ROLE_PROFESSOR).orElseGet(() -> {
                        RoleEntity r = new RoleEntity(Roles.ROLE_PROFESSOR);
                        return roleRepository.save(r);
                    });

                    CredentialsEntity professorCreds = CredentialsEntity.builder()
                            .username("profesor")
                            .password(passwordEncriptada)
                            .enabled(true)
                            .user(newProfe)
                            .build();
                    professorCreds.getRoles().add(roleProf);
                    credentialsRepository.save(professorCreds);

                    return newProfe;
                });

                // Obtener las actividades
                ActivityEntity actMusculacion = activityRepository.findAll().stream()
                        .filter(a -> "Musculación".equalsIgnoreCase(a.getName()))
                        .findFirst()
                        .orElse(null);

                ActivityEntity actSpinning = activityRepository.findAll().stream()
                        .filter(a -> "Spinning".equalsIgnoreCase(a.getName()))
                        .findFirst()
                        .orElse(null);

                ActivityEntity actCrossfit = activityRepository.findAll().stream()
                        .filter(a -> "Crossfit".equalsIgnoreCase(a.getName()))
                        .findFirst()
                        .orElse(null);

                // Si no se encuentran por nombre, tomar las primeras disponibles
                if (actMusculacion == null || actSpinning == null || actCrossfit == null) {
                    var allActs = activityRepository.findAll();
                    if (!allActs.isEmpty()) {
                        if (actMusculacion == null) actMusculacion = allActs.get(0);
                        if (actSpinning == null) actSpinning = allActs.size() > 1 ? allActs.get(1) : allActs.get(0);
                        if (actCrossfit == null) actCrossfit = allActs.size() > 2 ? allActs.get(2) : allActs.get(0);
                    }
                }

                // Crear clases
                if (actSpinning != null) {
                    GymClassEntity classSpinning = new GymClassEntity();
                    classSpinning.setActivity(actSpinning);
                    classSpinning.setProfessor(professor);
                    classSpinning.setDayOfWeek(DayOfWeek.MONDAY);
                    classSpinning.setStartTime(LocalTime.of(8, 0));
                    classSpinning.setEndTime(LocalTime.of(9, 0));
                    classSpinning.setCapacityMax(20);
                    classSpinning.setActive(true);
                    gymClassRepository.save(classSpinning);
                    System.out.println(">> Clase de Spinning sembrada.");
                }

                if (actCrossfit != null) {
                    GymClassEntity classCrossfit = new GymClassEntity();
                    classCrossfit.setActivity(actCrossfit);
                    classCrossfit.setProfessor(professor);
                    classCrossfit.setDayOfWeek(DayOfWeek.WEDNSEDAY); // Usar el enum WEDNSEDAY
                    classCrossfit.setStartTime(LocalTime.of(18, 0));
                    classCrossfit.setEndTime(LocalTime.of(19, 30));
                    classCrossfit.setCapacityMax(15);
                    classCrossfit.setActive(true);
                    gymClassRepository.save(classCrossfit);
                    System.out.println(">> Clase de Crossfit sembrada.");
                }

                if (actMusculacion != null) {
                    GymClassEntity classMusculacion = new GymClassEntity();
                    classMusculacion.setActivity(actMusculacion);
                    classMusculacion.setProfessor(professor);
                    classMusculacion.setDayOfWeek(DayOfWeek.FRIDAY);
                    classMusculacion.setStartTime(LocalTime.of(10, 0));
                    classMusculacion.setEndTime(LocalTime.of(12, 0));
                    classMusculacion.setCapacityMax(30);
                    classMusculacion.setActive(true);
                    gymClassRepository.save(classMusculacion);
                    System.out.println(">> Clase de Musculación sembrada.");
                }
            }

            System.out.println(">> ¡Verificación y sembrado de datos de prueba del gimnasio finalizados correctamente!");
        };
    }
}
