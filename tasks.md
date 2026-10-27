# Plan Maestro de Tareas — Gym System (tasks.md)

Este documento consolida **todas las Historias de Usuario y Subtareas existentes en el tablero de Jira**, integrando además las **tareas arquitectónicas y de negocio complementarias** necesarias para que el Frontend (Angular) y el Backend (Spring Boot) encastren al 100% y se logre la finalización total del proyecto.

---

## Índice de Épicas

- [Épica 0: Arquitectura Base, Core y Layout Shell (Infraestructura Frontend)](#épica-0-arquitectura-base-core-y-layout-shell)
- [Épica 1: Seguridad, Autenticación y Perfiles](#épica-1-seguridad-autenticación-y-perfiles)
- [Épica 2: Catálogo de Membresías y Cobros](#épica-2-catálogo-de-membresías-y-cobros)
- [Épica 3: Gestión de Clases y Asistencia](#épica-3-gestión-de-clases-y-asistencia)
- [Épica 4: Gestión de Rutinas y Seguimiento](#épica-4-gestión-de-rutinas-y-seguimiento)
- [Épica 5: Panel de Administración, Control y Auditoría](#épica-5-panel-de-administración-control-y-auditoría)

---

## Épica 0: Arquitectura Base, Core y Layout Shell

> **Objetivo:** Establecer los cimientos transversales de la aplicación Angular (interceptores, guards de rol, tema oscuro de alta gama y navegación responsiva).

### `[GS-CORE] Arquitectura Base, Interceptores HTTP y Route Guards (RBAC)`
- **Tipo:** Tarea Técnica / Historia Base
- **Descripción:**
  Como desarrollador del sistema, quiero implementar la infraestructura central de comunicación HTTP, manejo de tokens e interceptores en Angular para asegurar que todas las peticiones viajen autenticadas y las rutas estén protegidas según el rol del usuario.
- **Criterios de Aceptación:**
  - `HttpClient` configurado con `provideHttpClient(withFetch(), withInterceptors([jwtInterceptor, errorInterceptor]))` en `app.config.ts`.
  - `JwtInterceptor` inyecta automáticamente `Authorization: Bearer <token>` en todas las peticiones salientes (excepto `/api/auth/**`).
  - `ErrorInterceptor` captura respuestas `401 Unauthorized` (limpieza de sesión y redirección a `/login`), `403 Forbidden` y `409 Conflict`.
  - Guard funcional `CanActivateFn` para proteger rutas por rol (`ROLE_ADMIN`, `ROLE_PROFESSOR`, `ROLE_CLIENT`).
  - Layout Shell con sidebar/navbar responsivo siguiendo el sistema de diseño (superficies Nivel 0 `bg-[#09090b]` a Nivel 3, cero emojis, iconografía SVG).
- **Tareas de Desarrollo:**
  - [ ] `GS-CORE-1`: Configurar `app.config.ts` y estructura de interceptores funcionales (`jwt.interceptor.ts`, `error.interceptor.ts`).
  - [ ] `GS-CORE-2`: Implementar `auth.guard.ts` y `role.guard.ts` basados en `CanActivateFn` y señales reactivas.
  - [ ] `GS-CORE-3`: Construir el componente Layout Shell (Navbar / Sidebar responsivo) con tokens del Design System en `src/app/shared/layout/`.
  - [ ] `GS-CORE-4`: Configurar el enrutador principal en `app.routes.ts` con carga perezosa (*lazy loading*) de features.

### `[GS-INFRA-DB] Migración de Persistencia a PostgreSQL (Supabase)`
- **Tipo:** Tarea Técnica de Infraestructura
- **Estado:** Completada
- **Descripción:**
  Migrar el almacenamiento relacional de MySQL a PostgreSQL administrado en Supabase, configurando el driver JDBC `org.postgresql:postgresql`, optimizando el connection pooler (HikariCP) y adaptando la suite de pruebas con base H2 aislada.
- **Criterios de Aceptación:**
  - Driver PostgreSQL integrado en `pom.xml`.
  - Configuración de datasource y HikariCP en `application.yaml` con soporte SSL.
  - Generación automática de esquema relacional (`ddl-auto: update`) y semillas iniciales de seguridad y catálogo.
  - Aislamiento de tests en `src/test/resources/application.yaml` con H2.
- **Tareas de Desarrollo:**
  - [x] `GS-DB-1`: Reemplazar conector MySQL por driver PostgreSQL en `pom.xml`.
  - [x] `GS-DB-2`: Ajustar configuración de datasource, timeouts y pooling HikariCP para Supabase en `application.yaml`.
  - [x] `GS-DB-3`: Configurar perfil de pruebas y eliminar test suites desactualizadas.
  - [x] `GS-DB-4`: Documentar variables de entorno y conexión a Supabase en `README.md`.

### `[GS-DESIGN-KINETIC] Rediseño Visual "Stealth Kinetic Dark" & Login Glassmorphism`
- **Tipo:** Tarea Técnica de Diseño & UI
- **Estado:** Completada
- **Descripción:**
  Rediseñar integralmente la experiencia visual del sistema aplicando un diseño artesanal de alto rendimiento deportivo, fotografía cinematográfica en el login, modal con efecto de cristal ahumado (*glassmorphism*), nuevo logo SVG de mancuerna/pesa y sistema de tokens en Vanilla CSS.
- **Criterios de Aceptación:**
  - Tokens CSS personalizados con paleta de grafito profundo, titanio y acentos Kinetic Amber (`styles.css`).
  - Fotografía cinematográfica real de fondo de gimnasio sin placeholders (`gym_dark_cinematic_bg.jpg`).
  - Modal de login translúcido con `backdrop-filter: blur(28px) saturate(190%)` y specular highlights.
  - Isotipo SVG geométrico de pesa olímpica con acabado metálico.
  - Armonización de tipografía `Outfit` y `Plus Jakarta Sans` en `index.html` y barras de navegación.
- **Tareas de Desarrollo:**
  - [x] `GS-DESIGN-1`: Generar asset fotográfico cinematográfico y configurar almacenamiento en `public/assets/`.
  - [x] `GS-DESIGN-2`: Importar Google Fonts `Outfit` e implementar tokens globales en `styles.css`.
  - [x] `GS-DESIGN-3`: Rediseñar componente Login con capas de viñeteado, modal vidrioso e isotipo de pesa (`login.html`, `login.css`).
  - [x] `GS-DESIGN-4`: Armonizar navbars y branding en vistas de usuario y administración.
  - [x] `GS-DESIGN-5`: Refinar formulario de login con enfoque minimalista, sobrio y sin sobrecarga decorativa.

---

## Épica 1: Seguridad, Autenticación y Perfiles

> **Épica Jira:** *Centralizar el control de acceso multinivel y la seguridad de navegación de la aplicación web mediante tokens JWT y Angular Guards, permitiendo a su vez la autogestión segura de datos personales del cliente.*

### `GS-5: Inicio y Cierre de Sesión Multinivel (JWT)`
- **Tipo:** Historia
- **Estado:** Completada
- **Descripción:**
  Como usuario del sistema (Administrador, Profesor, Cliente), quiero iniciar y cerrar sesión en una interfaz web reactiva para acceder de forma segura a las funciones correspondientes a mi rol.
- **Criterios de Aceptación:**
  - Formulario de login reactivo (`ReactiveFormsModule`) que envíe credenciales a `POST /api/auth/login`.
  - Almacenamiento seguro del token JWT en el cliente y redirección según el rol decodificado.
  - Manejo de errores visuales ante credenciales inválidas (`HTTP 401`).
  - Cierre de sesión que limpie el almacenamiento local y redirija a `/login`.
- **Subtareas Jira:**
  - [x] `GS-7`: Crear `AuthService` y DTOs de autenticación (`auth.service.ts`, `auth.models.ts`).
  - [x] `GS-8`: Diseñar componente Login con ReactiveForms (`login.ts`, `login.html`, `login.css`).
  - [x] `GS-9`: Implementar almacenamiento y decodificación de Token JWT (gestión de señales de sesión).
  - [x] `GS-10`: Crear e implementar `JwtInterceptor`.

### `GS-11: Autogestión de Perfil del Cliente`
- **Tipo:** Historia
- **Estado:** Completado
- **Descripción:**
  Como Cliente, quiero actualizar parcialmente mis datos personales (teléfono y fecha de nacimiento) para mantener mi información al día.
- **Criterios de Aceptación:**
  - Formulario que consuma el endpoint seguro `PATCH /api/users/me`.
  - Los campos bloqueados (nombre de usuario, DNI, rol) deben permanecer de solo lectura.
- **Subtareas Jira:**
  - [x] `GS-12`: Crear `ProfileComponent` y servicio de actualización (`profile.service.ts`).
  - [x] `GS-13`: Integrar formulario reactivo para patch de datos personales.
  - [x] `GS-14`: Probar la persistencia parcial de datos de usuario con el backend.

### `[GS-AUTH-EXT] Actualización Obligatoria de Clave y Nombre de Usuario Temporal`
- **Tipo:** Historia Complementaria
- **Descripción:**
  Como socio o profesor que ingresa por primera vez con credenciales asignadas por defecto (DNI), quiero actualizar obligatoriamente mi contraseña y definir mi nombre de usuario definitivo para activar mi cuenta y cumplir con la regla de negocio del gimnasio.
- **Criterios de Aceptación:**
  - Detección reactiva en el login si el usuario posee credenciales temporales.
  - Bloqueo de navegación hasta completar la actualización.
  - Formulario reactivo para cambio de clave enviado a `PATCH /api/users/change-password`.
  - Formulario reactivo para cambio de nombre de usuario enviado a `PATCH /api/users/change-username`.
- **Tareas de Desarrollo:**
  - [ ] `GS-AUTH-EXT-1`: Crear componente modal / vista de primer login (`change-credentials.ts`).
  - [ ] `GS-AUTH-EXT-2`: Conectar endpoints de cambio de clave y username en `UserService`.

---

## Épica 2: Catálogo de Membresías y Cobros

> **Épica Jira:** *Catálogo de Membresías y Cobros*

### `GS-15: Catálogo y Contratación de Membresías`
- **Tipo:** Historia
- **Estado:** Por hacer
- **Descripción:**
  Como Cliente, quiero consultar los planes de membresía disponibles y adquirir uno para habilitar mi acceso al gimnasio.
- **Criterios de Aceptación:**
  - Listado dinámico de planes consumiendo `GET /api/membership/plans` con desglose de precio y duración.
  - Flujo de contratación vía `POST /api/membership/subscriptions` que calcule y muestre la fecha de finalización calculada.
  - Bloqueo visual y notificación de conflicto (`HTTP 409`) si el usuario ya posee una membresía en estado `ACTIVE`.
- **Subtareas Jira:**
  - [ ] `GS-20`: Crear `MembershipService` y modelos de planes (`membership.models.ts`).
  - [ ] `GS-21`: Diseñar vista de catálogo de membresías en tarjetas con tokens de diseño.
  - [ ] `GS-22`: Implementar contratación de plan y validación de suscripción activa (`GET /api/membership/my-status`).

### `GS-23: Registro de Cobro y Consulta de Pagos`
- **Tipo:** Historia
- **Estado:** Por hacer
- **Descripción:**
  Como Cliente, quiero registrar el pago de mi suscripción (efectivo/tarjeta) y consultar mis comprobantes históricos para llevar el control de mis cuotas.
- **Criterios de Aceptación:**
  - Formulario para simular el pago (`EFECTIVO` o `TARJETA`) llamando a `POST /api/membership/payments`.
  - Vista tabular que liste el historial de transacciones del cliente autenticado.
- **Subtareas Jira:**
  - [ ] `GS-24`: Diseñar componente de pago (Efectivo / Tarjeta).
  - [ ] `GS-25`: Crear tabla de historial de pagos del cliente.
  - [ ] `GS-26`: Conectar la vista de pagos con `MembershipService`.

### `[GS-MEMB-ADMIN] Administración y Actualización de Planes Comerciales (Admin)`
- **Tipo:** Historia Complementaria
- **Descripción:**
  Como Administrador, quiero crear y actualizar los planes de membresía del gimnasio (precios y duración en días) para mantener vigente la oferta comercial.
- **Criterios de Aceptación:**
  - Tabla de administración de planes exclusiva para `ROLE_ADMIN`.
  - Formulario reactivo para alta y edición de planes con validaciones numéricas (`price > 0`, `duration_days > 0`) enviando a `POST /api/membership/plans` o `PUT /api/membership/plans/{publicId}`.
- **Tareas de Desarrollo:**
  - [ ] `GS-MEMB-ADMIN-1`: Crear componente de gestión de planes para admin (`admin-plans.ts`).
  - [ ] `GS-MEMB-ADMIN-2`: Integrar métodos CRUD de planes en `MembershipService`.

---

## Épica 3: Gestión de Clases y Asistencia

> **Épica Jira:** *Gestión de Clases y Asistencia*

### `GS-27: Visualización de Agenda y Cupos en Tiempo Real`
- **Tipo:** Historia
- **Estado:** Por hacer
- **Descripción:**
  Como Cliente o Profesor, quiero consultar las clases disponibles con sus cupos libres en tiempo real y filtrarlas para planificar mis horarios.
- **Criterios de Aceptación:**
  - Listado de clases consumiendo `GET /api/activities/classes` calculando los cupos libres (`capacity_max` menos inscripciones por fecha).
  - Filtros interactivos por día de la semana y por disciplina.
- **Subtareas Jira:**
  - [ ] `GS-28`: Crear `ClassService` y DTOs de agenda (`class.models.ts`).
  - [ ] `GS-29`: Desarrollar interfaz de calendario semanal de clases.
  - [ ] `GS-30`: Implementar barra de filtros reactiva por disciplina y día de la semana usando `computed()`.

### `GS-31: Inscripción y Cancelación de Asistencia`
- **Tipo:** Historia
- **Estado:** Por hacer
- **Descripción:**
  Como Cliente con membresía activa, quiero inscribirme a una clase o cancelar mi reserva para gestionar mi asistencia.
- **Criterios de Aceptación:**
  - Inscripción vía `POST /api/activities/enrollments` sujeta a validación: denegar con `HTTP 403` si la membresía no está `ACTIVE`, o con `HTTP 409` si el cupo está agotado.
  - Botón para cancelar inscripción vía `DELETE /api/activities/enrollments/{id}`, actualizando inmediatamente el aforo visual.
- **Subtareas Jira:**
  - [ ] `GS-32`: Implementar lógica de inscripción y validación de membresía en cliente.
  - [ ] `GS-33`: Agregar badges visuales de cupos y botones de reserva / cancelación.
  - [ ] `GS-34`: Manejar alertas semánticas `HTTP 403` y `HTTP 409` en inscripciones.

### `[GS-CLASS-ADMIN] Gestión de Clases, Disciplinas y Asistencia (Profesor/Admin)`
- **Tipo:** Historia Complementaria
- **Descripción:**
  Como Profesor o Administrador, quiero crear, editar clases y consultar el listado de alumnos inscriptos para coordinar la agenda deportiva del complejo.
- **Criterios de Aceptación:**
  - Formulario para crear clases con profesor, actividad, día y horario (`POST /api/classes`).
  - Validación ante superposición horaria del profesor (`HTTP 409`).
  - Modal para visualizar la nómina de clientes matriculados por clase (`GET /api/activities/classes/{id}/enrollments`).
- **Tareas de Desarrollo:**
  - [ ] `GS-CLASS-ADMIN-1`: Crear componente de administración de clases y actividades (`class-admin.ts`).
  - [ ] `GS-CLASS-ADMIN-2`: Implementar modal con la lista de socios inscriptos por clase.

---

## Épica 4: Gestión de Rutinas y Seguimiento

> **Épica Jira:** *Gestión de Rutinas y Seguimiento*

### `GS-35: Gestión y Asignación de Rutinas Personalizadas`
- **Tipo:** Historia
- **Estado:** Por hacer
- **Descripción:**
  Como Profesor, quiero crear, listar, modificar y eliminar rutinas asignando ejercicios con series, repeticiones y peso a los clientes para personalizar su entrenamiento.
- **Criterios de Aceptación:**
  - CRUD completo de rutinas (pantallas de alta, edición, baja lógica y listado).
  - Selector dinámico de ejercicios maestros clasificados por grupo muscular.
  - Envío de carga estructurada (`POST /api/routines`) con arrays anidados de ejercicios.
- **Subtareas Jira:**
  - [ ] `GS-36`: Crear `RoutineService` y modelos de ejercicios (`routine.models.ts`).
  - [ ] `GS-37`: Desarrollar formulario dinámico con `FormArray` para asignación de series, repeticiones y cargas.
  - [ ] `GS-38`: Implementar tabla CRUD de rutinas para profesores con búsqueda y filtros.

### `GS-39: Consulta de Rutina Vigente`
- **Tipo:** Historia
- **Estado:** Por hacer
- **Descripción:**
  Como Cliente, quiero consultar en pantalla mi rutina activa con el detalle de ejercicios asignados para ejecutar mi entrenamiento diario.
- **Criterios de Aceptación:**
  - Consumo del endpoint `GET /api/routines/my-routine`.
  - Renderizado estructurado de los ejercicios, series, repeticiones y cargas en vista responsive adaptada a móviles.
- **Subtareas Jira:**
  - [ ] `GS-40`: Diseñar vista de rutina del cliente en tarjetas móviles (`my-routine.ts`, `my-routine.html`).
  - [ ] `GS-41`: Manejar estado vacío cuando el cliente no posee una rutina vigente asignada.

### `[GS-EXERCISE-CRUD] Catálogo Maestro de Ejercicios por Grupo Muscular`
- **Tipo:** Historia Complementaria
- **Descripción:**
  Como Profesor, quiero dar de alta y consultar ejercicios maestros clasificados por zona muscular (`PECHO`, `ESPALDA`, `PIERNAS`, `BRAZOS`, `CORE`) para alimentar el creador de rutinas.
- **Criterios de Aceptación:**
  - Catálogo filtrable por grupo muscular consumiendo `GET /api/exercises`.
  - Formulario reactivo para alta de ejercicios vía `POST /api/exercises`.
- **Tareas de Desarrollo:**
  - [ ] `GS-EXERCISE-CRUD-1`: Crear componente de catálogo de ejercicios (`exercises.ts`).
  - [ ] `GS-EXERCISE-CRUD-2`: Integrar métodos en `ExerciseService`.

---

## Épica 5: Panel de Administración, Control y Auditoría

> **Épica Jira:** *Panel de Administración y Control*

### `GS-42: Panel Administrativo y CRUD de Usuarios`
- **Tipo:** Historia
- **Estado:** Completada
- **Descripción:**
  Como Administrador, quiero dar de alta, editar, listar y habilitar/deshabilitar cuentas de profesores y clientes mediante su UUID para gestionar los accesos a la plataforma.
- **Criterios de Aceptación:**
  - CRUD completo de usuarios (formulario de alta, edición y listado general).
  - Conmutador para activar/desactivar cuentas vía `PATCH /api/users/{publicId}/status`.
- **Subtareas Jira:**
  - [x] `GS-43`: Crear `AdminUserService` y DTOs de gestión de usuarios.
  - [x] `GS-44`: Diseñar modal de alta y edición de socios y staff (profesores/admin).
  - [x] `GS-45`: Integrar toggle de estado con `PATCH /api/users/{publicId}/status`.

### `GS-46: Auditoría de Precios y Dashboard de Métricas (Chart.js)`
- **Tipo:** Historia
- **Estado:** Por hacer
- **Descripción:**
  Como Administrador, quiero visualizar la bitácora de cambios de precios auditados por el backend y gráficos de asistencia/cobros para monitorear el desempeño del gimnasio.
- **Criterios de Aceptación:**
  - Tabla de consulta de logs de auditoría generados por el aspecto AOP (`GET /api/audit/logs`).
  - Panel de métricas interactivo utilizando `Chart.js` (`ng2-charts`) para representar cobros y aforo de clases.
- **Subtareas Jira:**
  - [ ] `GS-47`: Instalar y configurar `chart.js` y `ng2-charts` en Angular.
  - [ ] `GS-48`: Crear componente Dashboard con gráficos estadísticos de ocupación y recaudación.
  - [ ] `GS-49`: Implementar tabla de visor forense de auditoría AOP con detalles JSON.

---

## Resumen Numérico del Proyecto

| Categoría | Cantidad | Detalle |
| :--- | :---: | :--- |
| **Épicas Funcionales** | 6 | Infraestructura, Seguridad, Membresías, Clases, Rutinas, Auditoría |
| **Historias Jira Originales** | 10 | `GS-5`, `GS-11`, `GS-15`, `GS-23`, `GS-27`, `GS-31`, `GS-35`, `GS-39`, `GS-42`, `GS-46` |
| **Subtareas Jira Originales** | 27 | Tareas técnicas desglosadas en el tablero |
| **Historias Complementarias** | 5 | Setup Core, Cambio credenciales, Admin planes, Admin clases, Maestro ejercicios |
| **Tareas Técnicas Complementarias** | 12 | Subtareas de soporte para completar el 100% del sistema |
| **Total de Ítems de Trabajo** | **54** | Plan completo para finalización del proyecto |
