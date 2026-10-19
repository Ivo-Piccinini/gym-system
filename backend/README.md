# Gym System - API

Este proyecto es el backend de gestión para el **Gym System**, desarrollado bajo el framework **Spring Boot** (Java 21) utilizando una arquitectura limpia modular orientada a características (**Package by Feature**), seguridad jerárquica con **Spring Security + JWT**, y persistencia relacional con **Spring Data JPA** y **MySQL**.

---

## Problemáticas que Resuelve

El sistema está diseñado para resolver los desafíos comunes de administración y operación de un gimnasio:
1. **Falta de Control de Accesos e Inscripciones:** Previene la inscripción de socios inactivos o sin suscripción activa a las clases grupales.
2. **Superposición Horaria de Profesores:** Valida automáticamente que un profesor no pueda tener asignada más de una clase en el mismo día y rango horario.
3. **Pérdida de Historial e Auditoría:** Registra cada acción administrativa y de negocio sensible (altas, pagos, inscripciones, cambios de clave) utilizando Programación Orientada a Aspectos (AOP) para una auditoría forense descentralizada.
4. **Vulnerabilidad de Enumeración de IDs:** Protege los recursos exponiendo exclusivamente identificadores públicos únicos (`UUID`) al exterior (APIs, Frontend), ocultando las claves primarias numéricas secuenciales internas (`Long`).

---

## Reglas de Negocio Implementadas

* **Jerarquía de Accesos (Roles):**
  * `ROLE_ADMIN`: Acceso total, incluyendo el ABM de staff (profesores y administradores) y visualización global de pagos y suscripciones.
  * `ROLE_PROFESSOR`: Gestión de agenda de clases, actividades y catálogo de ejercicios y rutinas.
  * `ROLE_CLIENT`: Compra de membresías, pagos, inscripciones a clases y visualización de su rutina activa asignada.
* **Control de Cupos y Capacidad:** La inscripción del cliente a una clase grupal resta un cupo de la capacidad máxima autorizada (`capacity_max`). No permite inscripciones si el límite es alcanzado.
* **Vigencia de Suscripción:** Bloquea la inscripción a cualquier clase si la membresía del cliente está en estado `EXPIRED`, `PENDING` o `CANCELED`.
* **Personalización Única de Cuenta:** Al crearse un usuario por el administrador, se le asigna su DNI como contraseña y nombre de usuario temporal. El sistema exige al cliente cambiar su nombre de usuario por única vez y actualizar su clave al ingresar.

---

## Tecnologías y Estructura

* **Core:** Java 21, Spring Boot 4.0.0
* **Seguridad:** Spring Security, JWT (JSON Web Tokens), BCrypt Password Encoder
* **Base de Datos:** Hibernate / JPA, MySQL
* **AOP:** Spring AOP (AspectJ) para el log automatizado de auditorías en la tabla `audit_logs`.
* **Documentación:** Swagger UI (OpenAPI 3.0)

---

## Pasos para Ejecutar el Proyecto

### 1. Requisitos Previos
* **Java 21 JDK** instalado.
* **MySQL Server** en ejecución.
* Crear una base de datos vacía en MySQL (ej. `gym_system_db`).

### 2. Configurar Variables de Entorno
Crea las siguientes variables de entorno en tu sistema o configúralas en tu archivo `application.yaml`:
* `DB_URL`: URL de conexión a tu base de datos MySQL (ej. `jdbc:mysql://localhost:3306/gym_system_db`)
* `DB_USER`: Tu usuario de base de datos MySQL (ej. `root`)
* `DB_PASSWORD`: Tu contraseña de base de datos MySQL.

### 3. Compilar y Ejecutar la Aplicación
Utiliza el Maven Wrapper provisto en la raíz del proyecto:
```bash
# Compilar el proyecto
./mvnw clean compile

# Ejecutar el servidor de desarrollo
./mvnw spring-boot:run
```

### 4. Seed Inicial de Datos
Al iniciar la aplicación por primera vez, el sistema detectará la base de datos limpia y creará automáticamente los permisos, roles y las siguientes cuentas de prueba:
* **Administrador:** `admin` (Contraseña: `password123`)
* **Clientes:** `cliente1` hasta `cliente9` (Contraseña: `password123`)

### 5. Documentación Interactiva (Swagger/OpenAPI)
Una vez que el servidor esté corriendo en el puerto por defecto (`8080`), puedes consultar el catálogo interactivo de endpoints ingresando a:
👉 [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
