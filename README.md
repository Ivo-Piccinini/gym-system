# Gym System

Monorepositorio para el sistema integral de gestión de gimnasio (**Gym System**), compuesto por:
- **Backend:** API RESTful desarrollada con Spring Boot (Java 21), Spring Security + JWT, MySQL y Spring Data JPA.
- **Frontend:** SPA interactiva desarrollada en Angular (Standalone components, arquitectura 2025/2026).

---

## Estructura del Proyecto

```text
gym-system/
├── backend/                  # API RESTful en Spring Boot
│   ├── src/                  # Código fuente Java y recursos
│   ├── pom.xml               # Configuración de dependencias Maven
│   ├── mvnw / mvnw.cmd       # Maven Wrapper
│   └── README.md             # Documentación detallada del backend
│
├── frontend/                 # Aplicación SPA en Angular
│   ├── src/                  # Componentes, vistas y lógica cliente
│   ├── angular.json          # Configuración de Angular CLI
│   ├── package.json          # Dependencias y scripts de npm
│   └── README.md             # Documentación del frontend
│
├── docs/                     # Documentación general y arquitectura
└── context.md                # Contexto y directrices de implementación frontend
```

---

## Cómo Ejecutar el Proyecto

### 1. Backend (Spring Boot)
Ingresa al directorio `backend/`:
```bash
cd backend
./mvnw spring-boot:run
```
Para más detalles sobre configuración de base de datos y variables de entorno, consulta [backend/README.md](file:///d:/Github/gym-system/backend/README.md).

### 2. Frontend (Angular)
Ingresa al directorio `frontend/`:
```bash
cd frontend
npm start
```
La aplicación estará disponible por defecto en [http://localhost:4200](http://localhost:4200).
