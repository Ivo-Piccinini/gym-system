# CONTEXT.MD — Gym System Frontend (Angular)

## 1. Visión y Objetivos del Proyecto

Desarrollar la interfaz SPA (*Single Page Application*) para **Gym System**, consumiendo la API RESTful desarrollada en Spring Boot (Java 21) con persistencia relacional MySQL y seguridad JWT.

El proyecto responde a los criterios de cátedra de Programación IV y gestión ágil (Jira), cubriendo las problemáticas de control de accesos, aforo en tiempo real, gestión de membresías, asignación de rutinas dinámicas y paneles de control/auditoría.

---

## 2. Convenciones de Programación y Guía de Estilo (Estándar Cátedra Angular 2025/2026)

Todo el código generado para el frontend debe seguir estrictamente estas reglas:

### 2.1 Estructura de Archivos y Nomenclatura

- **Arquitectura Standalone:** No se utiliza `NgModule` ni `app.module.ts`. Todos los componentes, directivas y pipes son Standalone (en Angular moderno ya no requiere declarar explícitamente `standalone: true`).
- **Nomenclatura sin sufijos en componentes:** Según la guía de estilo oficial actualizada, los componentes **no** llevan el sufijo `.component.ts`. Se nombran directamente en kebab-case:
  - Archivo de clase: `tarjeta-producto.ts` (en vez de `tarjeta-producto.component.ts`)
  - Template: `tarjeta-producto.html`
  - Estilos: `tarjeta-producto.css`
- **Organización Package by Feature:** Todo se agrupa por funcionalidad, nunca por tipo de archivo técnico:
  ```text
  src/app/
  ├── auth/
  │   ├── login.ts
  │   ├── login.html
  │   ├── auth.service.ts
  │   └── auth.models.ts
  ├── users/
  ├── routines/
  ├── classes/
  ├── memberships/
  └── shared/
  ```
- **Prohibición de utilitarios genéricos:** Prohibido crear carpetas genéricas con archivos tipo `utils.ts`.

### 2.2 Anatomía y Responsabilidades de Componentes

#### Separación de responsabilidades:
- **Componente:** Presenta datos y captura interacción del usuario. No habla directamente con HTTP ni implementa lógica de negocio compleja.
- **Servicio:** Maneja lógica de negocio, llamadas a la API REST (`HttpClient`), y estado compartido. Desconoce la interfaz visual.
- **Modelo/Interface:** Contratos de datos tipados (`interface` o `type`).

#### Buenas prácticas del componente:
- **Imports explícitos:** Cada componente debe listar en el array `imports: [...]` de `@Component` todo lo que su vista requiera (`RouterLink`, pipes, `ReactiveFormsModule`, etc.).
- **Encapsulamiento y modificadores:**
  - `protected`: Para propiedades o métodos accesibles solo desde el template HTML.
  - `readonly`: Para propiedades no reasignables.
- **Orden interno del componente:** Propiedades de Angular arriba (`inputs`, `outputs`, `injects`), estado reactivo en el medio y métodos de acción al final.
- **Manejo de colecciones y filtros:** Prohibido usar Pipes impuros de filtrado u ordenamiento. Toda derivación, orden o filtrado de listas se realiza en el componente usando `computed()`.

### 2.3 Formularios y Reactividad

- **Formularios transaccionales:** Para formularios complejos (ej. alta de rutinas con series dinámicas, pagos), utilizar Reactive Forms (`FormGroup`, `FormArray`, `FormControl`) o Signal Forms según la necesidad del flujo.
- **Feedback de validación:** Manejar feedback de validación claro en la UI (`invalid`, `touched`, `errors`).

---

## 3. Arquitectura de Seguridad y Manejo de API

### 3.1 Integración HTTP y Autenticación

- **HttpClient moderno:** Configurado en `app.config.ts` mediante `provideHttpClient(withFetch(), withInterceptors([...]))`.
- **JWT Interceptor:** Interceptor funcional que adjunta el encabezado `Authorization: Bearer <token>` a todas las peticiones salientes (excepto endpoints públicos `/api/auth/**`).
- **Error Handling Interceptor:** Redirecciona al login ante respuestas `401 Unauthorized` y maneja notificaciones amigables para:
  - `403 Forbidden` (ej. membresía inactiva)
  - `409 Conflict` (ej. cupo lleno o superposición horaria)

### 3.2 Guards y Control de Roles (RBAC)

Implementar `CanActivateFn` para proteger rutas según los roles expuestos en el token JWT:
- `ROLE_ADMIN`: Acceso total, ABM de usuarios/staff, visor de auditoría AOP y dashboard de métricas.
- `ROLE_PROFESSOR`: Gestión de clases, visualización de inscriptos y CRUD maestro de rutinas.
- `ROLE_CLIENT`: Compra/simulación de membresías, reserva y cancelación de clases, y consulta de su rutina activa (`/my-routine`).

---

## 4. Mapeo Funcional y Rutas del Sistema (Backlog Jira)

### Módulo 1: Auth & User Management (Épica 1 y 5)

#### Rutas:
- `/login` (Público)
- `/perfil` (Cliente / Profesor / Admin)
- `/admin/usuarios` (Solo `ROLE_ADMIN`)

#### Endpoints Backend:
- `POST /api/auth/login` (Body: `{ username, password }`, Retorna: `{ token, roles, publicId }`)
- `POST /api/auth/register`
- `GET /api/users/me` o `PUT /api/users/{username}` (Datos personales)
- `GET /api/users` (Listado paginado)
- `PATCH /api/users/{publicId}/status` (Habilitación/deshabilitación)

### Módulo 2: Membresías y Cobros (Épica 2)

#### Rutas:
- `/planes` (Visualización de catálogo de membresías)
- `/pagos` (Simulación de pago y tabla histórica de comprobantes)

#### Endpoints Backend:
- `GET /api/membership/plans`
- `POST /api/membership/subscriptions`
- `POST /api/membership/payments` (Simulación de pago: `EFECTIVO` / `TARJETA`)
- `GET /api/membership/my-status`

### Módulo 3: Clases, Agenda y Cupos (Épica 3)

#### Rutas:
- `/clases` (Agenda semanal interactiva y selector de día/disciplina)
- `/mis-clases` (Reservas activas del cliente)

#### Endpoints Backend:
- `GET /api/activities` (Disciplinas: Yoga, Crossfit, Musculación)
- `GET /api/activities/classes?dayOfWeek=&activityId=` (Retorna cupos calculados: `capacity_max - enrollments`)
- `POST /api/activities/enrollments` (Reserva con validación de membresía y aforo)
- `DELETE /api/activities/enrollments/{id}` (Cancelación y liberación instantánea de cupo)

### Módulo 4: Rutinas y Entrenamientos (Épica 4 - CRUD Complejo)

#### Rutas:
- `/profesor/rutinas` (CRUD completo para `ROLE_PROFESSOR`: listado, alta y edición)
- `/mi-entrenamiento` (Vista responsive de tarjetas para `ROLE_CLIENT`)

#### Endpoints Backend:
- `GET /api/exercises` (Catálogo maestro por grupo muscular)
- `GET /api/routines` (Listado y filtros)
- `POST /api/routines` (Envío jerárquico con `exercises: [{ exerciseId, series, repetitions, weight }]`)
- `GET /api/routines/my-routine` (Visualización de rutina activa del cliente logueado)

### Módulo 5: Auditoría y Métricas (Épica 5)

#### Rutas:
- `/admin/dashboard` (Métricas visuales)
- `/admin/auditoria` (Bitácora forense de cambios de precios y acciones sensibles)

#### Endpoints Backend:
- `GET /api/audit/logs` (Visualización de registros creados por Spring AOP)

#### Librería de Gráficos:
- Integración con Chart.js (`ng2-charts`) para gráficos de ocupación de salones y recaudación mensual.

---

## 5. Directivas de Ejecución para el Asistente en Antigravity

- **Regla de Nomenclatura:** Nunca uses `.component.ts` al crear un componente; usa la convención directa `nombre-componente.ts`.
- **Importaciones y Standalone:** Asegura que cada componente tenga su bloque `imports: [...]` completo con las dependencias necesarias.
- **Manejo de Errores Semánticos:** En las suscripciones HTTP de los componentes o servicios, captura y muestra alertas específicas cuando el backend responda:
  - `403 Forbidden`: Membresía vencida o inactiva (prohibiendo la reserva).
  - `409 Conflict`: Cupo agotado o conflicto horario.
- **Diseño Mobile-First & Responsive:** Utilizar maquetado responsivo (CSS Flexbox/Grid o utilidades Tailwind/Bootstrap) adaptable a smartphones (clave para el cliente en el gimnasio) y pantallas de escritorio (administración y agenda).


## Sistema de Diseño y Pautas de Estilos

### 1. Filosofía Visual y Principios Rectores
- **Estética Contemporánea y Sofisticada**: La interfaz se construye sobre una base de tonos profundos y neutros (zinc/slate de alta gama, evitando negros puros `#000000` planos salvo en contrastes intencionales).
- **Sensación de Profundidad por Capas**: La jerarquía espacial no depende de sombras proyectadas intensas, sino de la elevación tonal por capas de superficie (superficie base, contenedor, tarjeta, flotante).
- **Cero Emojis en UI**: Queda prohibido el uso de emojis en componentes, botones, encabezados o modales. Toda representación gráfica se realiza mediante iconografía vectorial SVG limpia, consistente y estilizada.

---

### 2. Jerarquía de Superficies y Elevación
Se debe mantener una progresión tonal estricta para guiar la atención visual:

| Nivel | Rol en UI | Ejemplos de Tokens / Clases |
| :--- | :--- | :--- |
| **Nivel 0 (Base)** | Fondo principal de la aplicación / Canvas | `bg-[#09090b]` o `bg-zinc-950` |
| **Nivel 1 (Superficie)** | Contenedores primarios, barras laterales, tarjetas base | `bg-zinc-900` o `bg-[#121215]` |
| **Nivel 2 (Elevada)** | Modales, popovers, menús desplegables, tarjetas anidadas | `bg-zinc-800/90` o `bg-[#1c1c21]` |
| **Nivel 3 (Interacción)** | Estados hover, elementos activos, inputs seleccionados | `bg-zinc-800` o `bg-zinc-700/60` |

---

### 3. Tipografía y Escala de Contraste
Para garantizar máxima legibilidad sin saturar la vista, el texto debe graduarse según su importancia:

- **Títulos y Encabezados Primarios**: Blanco suave o casi blanco (`text-zinc-100` / `#f4f4f5`).
- **Cuerpo de Texto y Datos Primarios**: Alto contraste legible (`text-zinc-300` / `#d4d4d8`).
- **Metadatos, Etiquetas y Subtítulos**: Contraste medio (`text-zinc-400` / `#a1a1aa`).
- **Placeholders y Estados Deshabilitados**: Contraste sutil pero accesible (`text-zinc-500` / `#71717a`).

---

### 4. Bordes, Delimitadores y Efectos Visuales
- **Bordes Finos y Translúcidos**: Utilizar bordes de 1px con opacidades sutiles para delimitar tarjetas e inputs (`border border-white/10` o `border-zinc-800/80`).
- **Glassmorphism y Desenfoque**: Para barras de navegación fijas, encabezados o fondos de modales, aplicar transparencias con desenfoque (`backdrop-blur-md bg-zinc-950/70`).
- **Resplandores y Luces Direccionales**: Para destacar elementos clave o estados activos, emplear resplandores suaves (`shadow-[0_0_20px_rgba(...,0.15)]`) o anillos de enfoque sutiles (`ring-1 ring-primary/40`).

---

### 5. Paleta de Acentos y Estados Semánticos
Los acentos nunca deben ser fluorescentes ni estridentes; se formulan combinando fondos translúcidos, bordes tenues y tipografía nítida:

- **Éxito (Success)**: Fondo suave `bg-emerald-500/10`, borde `border-emerald-500/20`, texto `text-emerald-400`.
- **Advertencia (Warning)**: Fondo suave `bg-amber-500/10`, borde `border-amber-500/20`, texto `text-amber-400`.
- **Peligro / Error (Destructive)**: Fondo suave `bg-rose-500/10`, borde `border-rose-500/20`, texto `text-rose-400`.
- **Informativo / Primario (Brand/Action)**: Fondo translúcido con acento en texto/borde o botones sólidos con texto contrastante de alto impacto.

---

### 6. Controles de Entrada y Microinteracciones
- **Inputs y Selectores**: Fondo ligeramente diferenciado (`bg-zinc-900/80`), borde tenue (`border-zinc-800`), y transición suave al enfoque (`focus:border-primary/50 focus:ring-1 focus:ring-primary/30`). Siempre con límites explícitos de longitud (`maxLength`).
- **Botones y Acciones**:
  - *Primario*: Fondo sólido con contraste contundente y respuesta a la pulsación (`active:scale-[0.98]`).
  - *Secundario / Fantasma*: Fondo transparente o `bg-zinc-900`, borde `border-white/10`, hover con elevación lumínica gradual (`hover:bg-zinc-800`).
