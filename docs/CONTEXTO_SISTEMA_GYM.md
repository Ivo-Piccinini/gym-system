markdown_content = """# Contexto Consolidado del Proyecto: Sistema Gym (Backend Spring Boot)

Este documento sirve como la **Fuente Única de Verdad (Single Source of Truth)** para el desarrollo, mantenimiento e integraciones avanzadas del backend del **Sistema Gym**. Consolida todos los requisitos funcionales, el diseño de la base de datos relacional (DER), las especificaciones de la API (OpenAPI), los estándares de validación de DTOs y la arquitectura interna bajo las directrices académicas de la UTN.

---

## 1. Visión General de la Arquitectura y Estándares Técnicos

El sistema está diseñado bajo una arquitectura modular y escalable que prioriza la seguridad, la auditoría y el desacoplamiento de capas.

### Patrones y Principios Clave:
* **Package by Feature:** El código fuente de Java se organiza en módulos funcionales autónomos (features). Cada módulo contiene sus propios controladores, servicios, repositorios, entidades y DTOs (ej. `com.utnGymGroup.gym_system.features.user`).
* **Ocultamiento de Identificadores Internos:** Para mitigar ataques de enumeración (inyección/manipulación de IDs secuenciales), el sistema maneja internamente claves primarias de tipo `Long` (`IDENTITY`), pero **exclusivamente** expone un `publicId` de tipo `UUID` hacia el exterior (APIs, Frontend).
* **Principio DRY (Don't Repeat Yourself):** Toda la lógica transversal (seguridad, manejo de excepciones, auditoría) está centralizada en componentes comunes utilizando Filtros de Spring Security y Programación Orientada a Aspectos (AOP).

---

## 2. Requisitos Funcionales Consolidados (RF)

El sistema soporta tres roles principales de usuarios con permisos jerárquicos: **Administrador (ADMIN)**, **Profesor (PROFESSOR)** y **Cliente (CLIENT)**.

### Módulo Auth & User (Seguridad y Perfiles)
* **RF01:** El sistema debe permitir al administrador, profesor y cliente iniciar sesión mediante credenciales únicas.
* **RF02:** El sistema debe permitir el cierre de sesión seguro e invalidación/limpieza del estado de la sesión.
* **RF03/04/05:** El sistema debe permitir al **Administrador** realizar el ABM completo (Alta, Baja, Modificación), listado y filtrado de **Profesores** (Cuentas de Staff).
* **RF06/07/08:** El sistema debe permitir al **Administrador** realizar el ABM completo, listado y filtrado de **Clientes**.
* **RF-Profile:** El sistema debe permitir a profesores y clientes visualizar y actualizar de forma parcial sus datos personales de perfil de manera autónoma.

### Módulo Membership (Planes y Suscripciones)
* **RF09:** El sistema debe permitir al **Administrador** visualizar e iterar sobre todas las suscripciones del gimnasio.
* **RF26:** El sistema debe permitir al **Cliente** pagar su suscripción eligiendo métodos como Efectivo o Tarjeta.
* **RF27:** El sistema debe permitir al **Cliente** visualizar su historial de suscripciones y el estado actual de la misma.
* **RF30:** El sistema debe permitir al **Cliente** solicitar la cancelación de su suscripción activa.
* **RF-Sec:** Bloqueo automatizado de accesos o inscripciones si el cliente posee una suscripción con estado `EXPIRED`, `PENDING` o `CANCELED`.

### Módulo Activities & Classes (Cupos y Agenda)
* **RF10:** El sistema debe permitir a profesores y clientes realizar búsquedas y filtrados avanzados sobre las clases programadas.
* **RF11/12/13:** El sistema debe permitir al **Profesor** realizar el ABM, listado y filtrado de los horarios y bloques de sus **Clases**.
* **RF14/15/16:** El sistema debe permitir al **Profesor** realizar el ABM, listado y filtrado de las **Actividades** matrices (ej. Yoga, CrossFit, Spinning).
* **RF24/25:** El sistema debe permitir al **Cliente** listar de forma transparente las actividades y clases disponibles en la agenda semanal.
* **RF28/29:** El sistema debe permitir al **Cliente** inscribirse en clases disponibles y visualizar su agenda de asistencias.
* **RF-Bus (Validación Crítica):** La inscripción de un cliente a una clase está estrictamente condicionada a:
    1. Que posea una suscripción `ACTIVE` vigente.
    2. Que la clase seleccionada no haya superado su capacidad máxima (`capacity_max`).

### Módulo Workout (Rutinas y Ejercicios)
* **RF17/18/19:** El sistema debe permitir al **Profesor** realizar el ABM, listado y filtrado de las **Rutinas** de entrenamiento creadas para los clientes.
* **RF20/21/22:** El sistema debe permitir al **Profesor** gestionar el maestro de **Ejercicios** (Alta, consulta y filtros por grupo muscular).
* **RF23:** El sistema debe permitir al **Profesor** asignar ejercicios específicos a una rutina, parametrizando valores de carga: series, repeticiones y peso.
* **RF31:** El sistema debe permitir al **Cliente** visualizar exclusivamente su rutina activa y vigente.

---

## 3. Modelo de Datos y Entidades JPA (DER)

A continuación se detalla la estructura relacional de la Base de Datos distribuida por módulos funcionales:

### Detalle de Tablas y Atributos

#### Módulo Auth & User
1.  **`roles`**
    * `id` (PK): `Long` (IDENTITY)
    * `public_id`: `UUID` (Unique, Not Null)
    * `name`: `VARCHAR(50)` (Valores: `ROLE_ADMIN`, `ROLE_PROFESSOR`, `ROLE_CLIENT`)
2.  **`users`**
    * `id` (PK): `Long` (IDENTITY)
    * `public_id`: `UUID` (Unique, Not Null)
    * `username`: `VARCHAR(50)` (Unique, Not Null)
    * `password`: `VARCHAR(255)` (Not Null - Encriptado con BCrypt)
    * `email`: `VARCHAR(100)` (Unique, Not Null)
    * `enabled`: `BOOLEAN` (Default: true)
3.  **`profiles`**
    * `id` (PK/FK): `Long` (Apunta a `users.id` - Relación `@OneToOne` compartiendo la clave primaria)
    * `public_id`: `UUID` (Unique, Not Null)
    * `dni`: `VARCHAR(20)` (Unique, Not Null)
    * `first_name`: `VARCHAR(50)` (Not Null)
    * `last_name`: `VARCHAR(50)` (Not Null)
    * `phone`: `VARCHAR(30)`
    * `birth_date`: `DATE`

#### Módulo Membership
4.  **`membership_plans`**
    * `id` (PK): `Long` (IDENTITY)
    * `public_id`: `UUID` (Unique)
    * `name`: `VARCHAR(50)` (ej. "Plan Pase Libre Mensual")
    * `price`: `DOUBLE` / `DECIMAL(10,2)`
    * `duration_days`: `INT`
5.  **`subscriptions`**
    * `id` (PK): `Long` (IDENTITY)
    * `public_id`: `UUID` (Unique)
    * `user_id` (FK): `Long` (Apunta a `users`)
    * `plan_id` (FK): `Long` (Apunta a `membership_plans`)
    * `start_date`: `DATE`
    * `end_date`: `DATE`
    * `status`: `VARCHAR(30)` (Valores: `ACTIVE`, `EXPIRED`, `PENDING`, `CANCELED`)
6.  **`payments`**
    * `id` (PK): `Long` (IDENTITY)
    * `public_id`: `UUID` (Unique)
    * `subscription_id` (FK): `Long` (Apunta a `subscriptions`)
    * `amount`: `DOUBLE`
    * `payment_date`: `TIMESTAMP`
    * `method`: `VARCHAR(30)` (Valores: `EFECTIVO`, `TARJETA`)

#### Módulo Activities & Classes
7.  **`activities`**
    * `id` (PK): `Long` (IDENTITY)
    * `public_id`: `UUID` (Unique)
    * `name`: `VARCHAR(50)`
    * `description`: `VARCHAR(250)`
8.  **`classes`**
    * `id` (PK): `Long` (IDENTITY)
    * `public_id`: `UUID` (Unique)
    * `activity_id` (FK): `Long` (Apunta a `activities`)
    * `professor_id` (FK): `Long` (Apunta a `users` con rol PROFESSOR)
    * `day_of_week`: `VARCHAR(20)` (ej. `LUNES`, `MARTES`)
    * `start_time`: `TIME`
    * `end_time`: `TIME`
    * `capacity_max`: `INT`
9.  **`enrollments`** (Tabla intermedia con comportamiento de entidad)
    * `id` (PK): `Long` (IDENTITY)
    * `public_id`: `UUID` (Unique)
    * `user_id` (FK): `Long` (Apunta a `users` con rol CLIENT)
    * `class_id` (FK): `Long` (Apunta a `classes`)
    * `enrollment_date`: `TIMESTAMP`

#### Módulo Workout
10. **`exercises`**
    * `id` (PK): `Long` (IDENTITY)
    * `public_id`: `UUID` (Unique)
    * `name`: `VARCHAR(50)`
    * `description`: `VARCHAR(250)`
    * `muscle_group`: `VARCHAR(50)` (ej. `PECHO`, `PIERNAS`)
11. **`routines`**
    * `id` (PK): `Long` (IDENTITY)
    * `public_id`: `UUID` (Unique)
    * `client_id` (FK): `Long` (Apunta a `users`)
    * `professor_id` (FK): `Long` (Apunta a `users`)
    * `name`: `VARCHAR(50)`
    * `start_date`: `DATE`
    * `end_date`: `DATE`
    * `type_routine`: `VARCHAR(50)` (ej. `HYPERTROPHY`, `CARDIO`, `HIIT`)
12. **`routine_exercises`** (Tabla intermedia con atributos de relación)
    * `id` (PK): `Long` (IDENTITY)
    * `exercise_id` (FK): `Long` (Apunta a `exercises`)
    * `routine_id` (FK): `Long` (Apunta a `routines`)
    * `series`: `INT`
    * `reps`: `INT`
    * `weight`: `INT` (en kg)

#### Módulo Admin/Audit
13. **`audit_logs`** (Tabla desacoplada para auditoría forense mediante AOP)
    * `id` (PK): `Long` (IDENTITY)
    * `action`: `VARCHAR(100)` (ej. `UPDATE_PLAN`, `DELETE_USER`)
    * `performed_by`: `VARCHAR(50)` (Username del operador)
    * `timestamp`: `TIMESTAMP`
    * `details`: `TEXT` o `JSON` (Estructura conteniendo los estados de la entidad antes y después)

---

## 4. API Rest: Catálogo de Endpoints (OpenAPI 3.0)

Todos los endpoints base están prefijados bajo la ruta global `/api/v1`.

### Módulo Auth & User
| Método | Endpoint | Acceso | Descripción |
| :--- | :--- | :--- | :--- |
| `POST` | `/auth/login` | Público | Autenticación de usuario. Retorna JWT y `AuthResponseDTO`. |
| `POST` | `/auth/register` | Público | Registro autónomo de clientes. Asigna automáticamente `ROLE_CLIENT`. |
| `GET` | `/users/me` | Autenticado | Obtiene los detalles de cuenta y perfil del usuario logueado. |
| `PATCH` | `/users/me` | Autenticado | Modificación parcial de datos personales en `ProfileEntity`. |
| `POST` | `/admin/users/staff` | `ADMIN` | Alta manual de usuarios internos con roles `PROFESSOR` o `ADMIN`. |
| `GET` | `/admin/profiles/{publicId}`| `ADMIN` | Obtiene el perfil detallado de cualquier usuario mediante su UUID. |

### Módulo Membership
| Método | Endpoint | Acceso | Descripción |
| :--- | :--- | :--- | :--- |
| `GET` | `/membership/plans` | Público | Lista todos los planes comerciales del gimnasio. |
| `POST` | `/membership/subscriptions`| `CLIENT` | Contrata/compra un plan utilizando el `publicId` del plan. |
| `POST` | `/membership/payments` | `CLIENT` / `ADMIN` | Registra la transacción de cobro vinculada a la suscripción. |
| `GET` | `/membership/my-status` | `CLIENT` | Retorna los días restantes y estado de la suscripción actual. |

### Módulo Activities & Classes
| Método | Endpoint | Acceso | Descripción |
| :--- | :--- | :--- | :--- |
| `GET` | `/activities` | Público | Lista el catálogo de disciplinas/actividades del complejo. |
| `GET` | `/activities/classes` | Público | Retorna la grilla horaria/agenda con cupos dinámicos en tiempo real. |
| `POST` | `/activities/enrollments` | `CLIENT` | Inscribe al cliente en una clase (Aplica validación de cupo y pago). |
| `DELETE`| `/activities/enrollments/{id}`| `CLIENT` | Cancela la asistencia liberando el cupo inmediatamente. |

### Módulo Workout
| Método | Endpoint | Acceso | Descripción |
| :--- | :--- | :--- | :--- |
| `GET` | `/exercises` | Autenticado | Lista el catálogo de ejercicios maestros disponibles. |
| `POST` | `/routines` | `PROFESSOR` | Crea una nueva rutina estructurada asignándola a un cliente. |
| `GET` | `/routines/my-routine` | `CLIENT` | Retorna la rutina actual vigente del cliente logueado. |

---

## 5. Diseño de DTOs y Reglas de Validación

Para estructurar datos limpios de entrada/salida y respetar las capas, se definen DTOs específicos utilizando **Jakarta Validation** (`jakarta.validation.constraints`).

### Estrategia de Validación por Grupos (Interfaces Marcadoras)
Para evitar crear múltiples DTOs duplicados para creación y actualización, se utilizan interfaces marcadoras en los controladores junto con la anotación `@Validated`:
* `com.utnGymGroup.gym_system.common.interfaces.ICreate`
* `com.utnGymGroup.gym_system.common.interfaces.IUpdate`

#### Ejemplo Arquitectónico: `UserDTO`
```java
public class UserDTO {
    
    @Null(groups = ICreate.class, message = "El id público debe ser generado por el servidor")
    @NotNull(groups = IUpdate.class, message = "El publicId es requerido para actualizar un recurso")
    private UUID publicId;

    @NotBlank(groups = {ICreate.class, IUpdate.class}, message = "El nombre de usuario no puede estar vacío")
    @Size(min = 4, max = 50, message = "El username debe tener entre 4 y 50 caracteres")
    private String username;

    @NotBlank(groups = ICreate.class, message = "La contraseña es requerida para el alta de cuenta")
    @Size(min = 8, message = "La contraseña debe poseer al menos 8 caracteres")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @NotBlank(groups = {ICreate.class, IUpdate.class}, message = "El correo electrónico es un campo mandatorio")
    @Email(message = "Debe proporcionar una estructura de correo válida")
    private String email;

    @NotNull(groups = ICreate.class, message = "El perfil con los datos personales es requerido")
    @Valid // Activa la validación en cascada dentro del ProfileDTO
    private ProfileDTO profile;

    @NotEmpty(groups = ICreate.class, message = "El usuario debe poseer al menos un rol asignado")
    @Valid
    private Set<RoleDTO> roles;
}
