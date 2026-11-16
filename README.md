# 🏋️ Gym System — Plataforma Integral de Gestión de Gimnasios

[![Java](https://img.shields.io/badge/Java-21-orange.svg?logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0-brightgreen.svg?logo=springboot)](https://spring.io/projects/spring-boot)
[![Angular](https://img.shields.io/badge/Angular-22-red.svg?logo=angular)](https://angular.dev/)
[![TypeScript](https://img.shields.io/badge/TypeScript-6.0-blue.svg?logo=typescript)](https://www.typescriptlang.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg?logo=postgresql)](https://www.postgresql.org/)
[![Swagger](https://img.shields.io/badge/OpenAPI-3.0-green.svg?logo=swagger)](https://swagger.io/)

**Gym System** es una solución integral y moderna diseñada para la administración, automatización y control operativo de centros de entrenamiento físico y gimnasios. 

El proyecto está construido como un **monorepositorio desacoplado** de alto rendimiento, compuesto por una **API RESTful modular en Spring Boot 4 (Java 21)** y una **Single Page Application (SPA) reactiva en Angular 22** con componentes standalone, tokens de diseño modernos y control de accesos basado en roles (RBAC).

---

## 📋 Tabla de Contenidos

1. [Características Principales](#-características-principales)
2. [Arquitectura y Estructura del Proyecto](#-arquitectura-y-estructura-del-proyecto)
3. [Requisitos Previos](#-requisitos-previos)
4. [Configuración de la Base de Datos](#-configuración-de-la-base-de-datos)
5. [Guía de Ejecución Paso a Paso](#-guía-de-ejecución-paso-a-paso)
   - [Paso 1: Clonar el Repositorio](#paso-1-clonar-el-repositorio)
   - [Paso 2: Configurar Variables de Entorno](#paso-2-configurar-variables-de-entorno)
   - [Paso 3: Levantar el Backend (Spring Boot)](#paso-3-levantar-el-backend-spring-boot)
   - [Paso 4: Levantar el Frontend (Angular)](#paso-4-levantar-el-frontend-angular)
6. [Cuentas y Credenciales de Prueba (Seed Inicial)](#-cuentas-y-credenciales-de-prueba-seed-inicial)
7. [Documentación de la API (Swagger UI)](#-documentación-de-la-api-swagger-ui)
8. [Pruebas y Validación de Calidad](#-pruebas-y-validación-de-calidad)
9. [Resolución de Problemas Frecuentes (Troubleshooting)](#-resolución-de-problemas-frecuentes-troubleshooting)
10. [Flujo de Trabajo y Buenas Prácticas (Gitflow)](#-flujo-de-trabajo-y-buenas-prácticas-gitflow)

---

## ✨ Características Principales

* 🔐 **Seguridad Jerárquica y RBAC:** Autenticación sin estado basada en **Spring Security** y tokens **JWT**, diferenciando tres perfiles clave:
  * **Administrador (`ROLE_ADMIN`):** Control total del gimnasio, gestión de staff (profesores y administradores), auditoría general, emisión y regeneración de credenciales QR y supervisión de cobros.
  * **Profesor (`ROLE_PROFESSOR`):** Planificación y gestión de clases grupales, asignación horaria, creación de rutinas personalizadas y catálogo de ejercicios categorizados por grupo muscular.
  * **Socio / Cliente (`ROLE_CLIENT`):** Consulta y contratación de planes de membresía, pago de cuotas, reserva de cupos en clases, visualización de rutinas activas y acceso a su credencial QR digital.
* 🛡️ **Protección de Identificadores (UUID):** Mitigación contra ataques de enumeración (IDOR). El sistema utiliza claves internas numéricas (`Long`), pero expone exclusivamente identificadores públicos únicos (`UUID`) en todos los endpoints REST.
* 👥 **Control Estricto de Cupos y Superposición:** Validación automática en tiempo real de capacidad máxima en clases y prevención de solapamientos horarios en la agenda de profesores.
* 🎫 **Control de Acceso Digital mediante Códigos QR:** Emisión automatizada de pases QR digitales (mediante la librería ZXing) exclusiva para socios con suscripción activa y pago aprobado.
* 📜 **Auditoría Forense con AOP:** Trazabilidad y registro descentralizado de acciones sensibles (altas, modificaciones, pagos, inicio de sesión) en una tabla dedicada de auditoría (`audit_logs`) mediante Spring AspectJ.
* 🎨 **Frontend Reactivo y Moderno:** Interfaz de usuario diseñada bajo el estándar Angular 22, arquitectura orientada a señales (`Signals`), standalone components, tema oscuro de alta gama y diseño completamente responsivo.

---

## 🏗️ Arquitectura y Estructura del Proyecto

El monorepositorio se organiza en dos capas independientes:

```text
gym-system/
├── backend/                       # API RESTful en Spring Boot (Java 21)
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/utnGymGroup/gym_system/
│   │   │   │   ├── common/        # Seguridad, JWT, excepciones globales, AOP, configuración inicial
│   │   │   │   └── features/      # Módulos Package-by-Feature (user, membership, class, routine, etc.)
│   │   │   └── resources/         # application.yaml y recursos estáticos
│   │   └── test/                  # Pruebas unitarias e integración (aisladas con H2)
│   ├── .env.example               # Plantilla de variables de entorno para la base de datos
│   ├── mvnw / mvnw.cmd            # Maven Wrapper multiplataforma (Linux/macOS y Windows)
│   ├── pom.xml                    # Gestión de dependencias y plugins Maven
│   └── README.md                  # Documentación técnica específica del backend
│
├── frontend/                      # SPA en Angular 22 (Standalone Components)
│   ├── src/
│   │   ├── app/                   # Features (auth, admin, classes, routines, membership, profile)
│   │   │   ├── core/              # Servicios de autenticación, interceptores HTTP (JWT/Error), guards
│   │   │   └── shared/            # Componentes reutilizables, layout shell, navbar/sidebar
│   │   └── environments/          # Configuración de entornos y URL base de la API
│   ├── package.json               # Dependencias de npm y scripts de ejecución
│   └── angular.json               # Configuración del compilador y build de Angular CLI
│
├── docs/                          # Documentación funcional, DER relacional y especificaciones
├── AGENTS.md                      # Reglas de desarrollo, convenciones de Git y commits
└── README.md                      # Guía general de instalación y ejecución del proyecto
```

---

## 💻 Requisitos Previos

Asegúrate de contar con las siguientes herramientas instaladas en tu computadora antes de comenzar:

| Herramienta | Versión Requerida | Comando de Verificación | Enlace de Descarga |
| :--- | :--- | :--- | :--- |
| **Java JDK** | **21 LTS** | `java -version` | [Eclipse Temurin 21](https://adoptium.net/) o [Amazon Corretto 21](https://aws.amazon.com/corretto/) |
| **Node.js** | **20.x o 22.x LTS** | `node -v` | [Node.js Oficial](https://nodejs.org/) |
| **npm** | **10.x u 11.x** | `npm -v` | Incluido junto con Node.js |
| **Git** | **2.x+** | `git --version` | [Git SCM](https://git-scm.com/) |
| **PostgreSQL** *(o Docker)* | **15.x o 16.x** | `psql --version` o `docker -v` | [PostgreSQL Oficial](https://www.postgresql.org/download/) / [Docker Desktop](https://www.docker.com/) |

> [!NOTE]
> No es obligatorio tener Maven ni Angular CLI instalados de forma global en el sistema operativo; el proyecto incluye **Maven Wrapper (`mvnw`)** para el backend y ejecuta Angular mediante las herramientas locales provistas en `package.json`.

---

## 🗄️ Configuración de la Base de Datos

El backend utiliza **PostgreSQL** mediante Spring Data JPA / Hibernate (`ddl-auto: update`), lo que significa que las tablas y restricciones se generan y actualizan automáticamente al arrancar.

Elige **una** de las siguientes tres opciones según tu preferencia:

### Opción A: Contenedor Docker (Recomendada y más rápida)
Si tienes Docker instalado, ejecuta el siguiente comando en tu terminal para levantar una base de datos PostgreSQL lista para usar en segundos:

```bash
docker run --name postgres-gym \
  -e POSTGRES_DB=gym_db \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  -d postgres:16-alpine
```

### Opción B: PostgreSQL Instalado Localmente
1. Abre tu gestor de PostgreSQL (pgAdmin, DBeaver o la consola `psql`).
2. Crea una base de datos llamada `gym_db`:
   ```sql
   CREATE DATABASE gym_db;
   ```
3. Verifica que tu usuario (`postgres`) y contraseña coincidan con la configuración que definirás en el archivo `.env`.

### Opción C: PostgreSQL en la Nube (Supabase / Neon)
Puedes utilizar una base de datos administrada gratuita en [Supabase](https://supabase.com/) o [Neon](https://neon.tech/). Solo necesitarás la URL de conexión JDBC con soporte SSL (modo `?sslmode=require`).

---

## 🚀 Guía de Ejecución Paso a Paso

Sigue estos sencillos pasos para tener todo el sistema funcionando en cualquier computadora:

### Paso 1: Clonar el Repositorio

Abre una terminal y clona el proyecto en tu máquina local:

```bash
git clone https://github.com/Ivo-Piccinini/gym-system.git
cd gym-system
```

---

### Paso 2: Configurar Variables de Entorno

El backend lee automáticamente las variables de entorno desde un archivo `.env` ubicado en el directorio `backend/` o desde las variables del sistema operativo.

1. Navega a la carpeta `backend/`:
   ```bash
   cd backend
   ```
2. Crea tu archivo `.env` a partir de la plantilla provista:
   * **En Linux / macOS / Git Bash:**
     ```bash
     cp .env.example .env
     ```
   * **En Windows (PowerShell):**
     ```powershell
     Copy-Item .env.example .env
     ```
   * **En Windows (CMD):**
     ```cmd
     copy .env.example .env
     ```
3. Edita el archivo `.env` con los datos de tu base de datos. Por ejemplo, para PostgreSQL local o Docker:
   ```env
   DB_URL=jdbc:postgresql://localhost:5432/gym_db
   DB_USER=postgres
   DB_PASSWORD=postgres
   ```
   *(Si usas Supabase o una base cloud con SSL, descomenta y completa las líneas correspondientes indicadas en `.env.example`).*

---

### Paso 3: Levantar el Backend (Spring Boot)

Estando dentro de la carpeta `backend/`, ejecuta el servidor de desarrollo utilizando el Maven Wrapper:

* **En Linux / macOS:**
  ```bash
  # Otorgar permisos de ejecución al script si es la primera vez
  chmod +x mvnw
  ./mvnw spring-boot:run
  ```
* **En Windows (PowerShell):**
  ```powershell
  .\mvnw.cmd spring-boot:run
  ```
* **En Windows (CMD):**
  ```cmd
  mvnw.cmd spring-boot:run
  ```

> [!TIP]
> **¿Qué hace el backend al iniciar?**
> 1. Se conecta a PostgreSQL y crea/actualiza automáticamente todas las tablas relacionales.
> 2. Ejecuta el sembrador inicial (`DatabaseInitializerConfig`), creando los permisos, roles del sistema, planes de membresía, catálogo inicial de ejercicios, actividades y usuarios de prueba.
> 3. El servidor quedará disponible y escuchando peticiones en: **`http://localhost:8080`**.

---

### Paso 4: Levantar el Frontend (Angular)

Abre una **segunda terminal** (mantén el backend corriendo en la primera) y dirígete a la carpeta `frontend/`:

1. Ingresa al directorio:
   ```bash
   cd frontend
   ```
2. Instala las dependencias del proyecto:
   ```bash
   npm install
   ```
3. Inicia el servidor de desarrollo de Angular:
   ```bash
   npm start
   ```
4. Abre tu navegador web favorito e ingresa a:
   👉 **`http://localhost:4200`**

El frontend se conectará de manera transparente con el backend en `http://localhost:8080/api` mediante la configuración definida en `frontend/src/environments/environment.ts`.

---

## 👤 Cuentas y Credenciales de Prueba (Seed Inicial)

Al inicializar la base de datos por primera vez, el sistema crea automáticamente las siguientes cuentas con datos de prueba listos para interactuar:

| Rol | Nombre de Usuario | Contraseña | Descripción y Alcance |
| :--- | :--- | :--- | :--- |
| **Administrador** | `admin` | `password123` | Control total del gimnasio: gestión de profesores y socios, visualización de auditoría y emisión de pases QR. |
| **Profesor** | `profesor` | `password123` | Gestión de la agenda semanal de clases, catálogo de actividades matrices y asignación de rutinas. |
| **Socio / Cliente** | `cliente1` | `password123` | Socio activo con historial: puede contratar planes, registrar pagos, reservar cupos y consultar su rutina y pase QR. |
| **Socios Adicionales** | `cliente2` a `cliente8` | `password123` | Cuentas de socios activas para pruebas simultáneas y validación de cupos máximos en clases. |
| **Socio Deshabilitado** | `cliente9` | `password123` | Cuenta inactiva de prueba para validar el bloqueo de accesos e inicio de sesión. |

---

## 📖 Documentación de la API (Swagger UI)

El backend incorpora documentación interactiva estandarizada bajo **OpenAPI 3.0 / Swagger UI**. Con el backend en ejecución, puedes explorar y probar todos los endpoints REST directamente desde tu navegador:

🔗 **Swagger UI:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)  
📄 **OpenAPI Docs (JSON):** [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

### Endpoints Principales Destacados:
* `POST /api/auth/login`: Autenticación de credenciales y generación del token JWT.
* `GET /api/users/profile`: Consulta de perfil del usuario autenticado.
* `GET /api/memberships`: Catálogo público de planes de suscripción disponibles.
* `POST /api/subscriptions/{planId}`: Contratación de membresía para socios.
* `POST /api/payments`: Registro de cobros y pagos de cuotas.
* `GET /api/classes`: Consulta de agenda y clases programadas con control de cupos.
* `POST /api/classes/{id}/enroll`: Inscripción de un socio a una clase grupal.
* `GET /api/routines/my-routine`: Consulta de la rutina de ejercicios activa del cliente.
* `POST /api/access/qr/generate/{userId}`: Emisión o renovación de credencial digital QR (solo administradores).
* `GET /api/access/qr/my-qr`: Obtención del código QR digital personal para ingreso al gimnasio.
* `GET /api/audit`: Consulta forense de eventos de auditoría (solo administradores).

---

## 🧪 Pruebas y Validación de Calidad

El proyecto cuenta con suites de pruebas automatizadas y validaciones de compilación para ambas capas:

### Backend:
Para ejecutar la suite de pruebas unitarias e integración con base de datos H2 en memoria:
```bash
cd backend
./mvnw test        # En Linux / macOS
.\mvnw.cmd test    # En Windows
```

### Frontend:
Para validar la compilación y los tipos de TypeScript sin levantar el servidor de desarrollo:
```bash
cd frontend
npm run build      # Valida tipos, bundles y templates
npm test           # Ejecuta pruebas unitarias con Vitest
```

---

## 🔧 Resolución de Problemas Frecuentes (Troubleshooting)

### 1. Error de conexión con la base de datos (`Connection refused` o `SocketTimeoutException`)
* **Causa:** El servicio de PostgreSQL no está corriendo o el puerto `5432` está bloqueado/ocupado.
* **Solución:**
  * Si usas Docker, verifica con `docker ps` que el contenedor `postgres-gym` esté en estado `Up`.
  * Si usas base local, asegúrate de que el servicio de PostgreSQL esté iniciado en tu sistema operativo (`services.msc` en Windows o `sudo systemctl status postgresql` en Linux).
  * Verifica que los valores de `DB_URL`, `DB_USER` y `DB_PASSWORD` en tu archivo `backend/.env` sean correctos.

### 2. Permiso denegado al ejecutar `./mvnw` en Linux o macOS
* **Causa:** El archivo `mvnw` no tiene permisos de ejecución asignados en sistemas UNIX.
* **Solución:** Ejecuta `chmod +x mvnw` dentro de la carpeta `backend/` y vuelve a intentar.

### 3. Puerto `8080` o `4200` ocupado
* **Causa:** Otra aplicación está utilizando el puerto del backend (`8080`) o del frontend (`4200`).
* **Solución:**
  * Para cambiar el puerto del backend, agrega `SERVER_PORT=8081` en tu `.env` o en `application.yaml`. *(Recuerda actualizar la variable `apiUrl` en `frontend/src/environments/environment.ts`).*
  * Para correr el frontend en otro puerto: `npx ng serve --port 4300`.

### 4. Incompatibilidad de versión de Node.js o npm
* **Causa:** Una versión obsoleta de Node.js (inferior a v20) generará errores con Angular 22.
* **Solución:** Actualiza a Node.js 20 LTS o 22 LTS descargándolo desde [nodejs.org](https://nodejs.org/) o gestionándolo mediante [NVM (Node Version Manager)](https://github.com/nvm-sh/nvm).

---

## 🌿 Flujo de Trabajo y Buenas Prácticas (Gitflow)

Este proyecto sigue una rigurosa convención de ramas y commits atómicos según lo establecido en [`AGENTS.md`](file:///d:/Github/gym-system/AGENTS.md):

* **Rama Principal:** `develop` es la rama base de integración continua. **Está prohibido hacer commits directos sobre `develop`**.
* **Ramas de Trabajo:** Se crean a partir de `develop` con nomenclatura `kebab-case`:
  * `feature/<nombre>`: Nuevas funcionalidades.
  * `fix/<nombre>`: Corrección de fallos o bugs.
  * `refactor/<nombre>`: Refactorizaciones internas.
  * `docs/<nombre>`: Documentación técnica y bitácoras.
* **Commits:** Mensajes obligatoriamente en español siguiendo Conventional Commits (ej. `feat(auth): implementar renovación de token`, `docs: actualizar readme del proyecto`).

---

<p align="center">
  Desarrollado con dedicación para una gestión ágil, moderna y segura de gimnasios. 🚀
</p>
