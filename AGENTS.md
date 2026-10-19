# Reglas del Proyecto — Gym System (AGENTS.md)

## Estructura de Ramas y Commits del Proyecto

### 1. Estructura de Ramas

#### `develop` (Rama Base de Integración):
- Es la rama principal de trabajo activo y desarrollo continuo.
- **Regla estricta:** Está prohibido hacer commits directos sobre `develop`. Todo cambio debe provenir de una rama de características/trabajo.

#### Ramas de Trabajo (`feature/*`, `fix/*`, etc.):
- Se crean siempre a partir de `develop`.
- Nomenclatura en formato `kebab-case` según el propósito:
  - `feature/<nombre-descriptivo>`: Nuevas funcionalidades o módulos.
  - `fix/<nombre-descriptivo>`: Corrección de errores o bugs.
  - `refactor/<nombre-descriptivo>`: Reestructuración o mejoras internas sin alterar comportamiento.
  - `docs/<nombre-descriptivo>`: Actualizaciones exclusivas de documentación técnica o bitácora.

---

### 2. Flujo de Trabajo (Gitflow de Desarrollo)

Para cada tarea o desarrollo se sigue el siguiente procedimiento estricto:

1. **Sincronización inicial:**
   ```bash
   git checkout develop
   git pull origin develop
   ```

2. **Creación de la rama de trabajo:**
   ```bash
   git checkout -b feature/<nombre-descriptivo>
   ```

3. **Desarrollo y validaciones locales obligatorias:**
   - **Cambios exclusivos en Frontend:** Ejecutar `npm run lint` y `npm run build` (dentro de `frontend/`).
   - **Cambios exclusivos en Backend:** Ejecutar `./mvnw test` o pruebas unitarias (o `python -m pytest backend/tests` según aplique al servicio).
   - **Cambios Full-Stack:** Ejecutar ambas suites de validación.

4. **Commits en la rama de trabajo:**
   - Registrar los cambios siguiendo las normas de commits atómicos.

5. **Subida de la rama de trabajo:**
   ```bash
   git push origin feature/<nombre-descriptivo>
   ```

6. **Integración en develop:**
   ```bash
   git checkout develop
   git merge feature/<nombre-descriptivo>
   git push origin develop
   ```

7. **Limpieza de ramas:**
   ```bash
   git branch -d feature/<nombre-descriptivo>
   git push origin --delete feature/<nombre-descriptivo>
   ```

8. **Estado final:** La terminal queda siempre posicionada en la rama `develop`.

---

### 3. Estructura y Convención de Commits

#### Formato General
Se sigue el estándar de Conventional Commits con los mensajes redactados obligatoriamente en español:

```text
<tipo>(<alcance_opcional>): <descripción concisa y en minúsculas>
```

#### Tipos de Commit Permitidos
- `feat`: Nueva funcionalidad para el usuario o sistema (ej. `feat(frontend): mostrar nombre de producto en detalle de venta`).
- `fix`: Corrección de un fallo o error (ej. `fix(forms): corregir validación de carga de archivos`).
- `docs`: Modificación o adición de documentación o bitácoras (ej. `docs: actualizar bitácora con feature de comisiones`).
- `refactor`: Refactorización de código que no añade funcionalidades ni corrige bugs.
- `test`: Adición o ajuste de pruebas unitarias o de integración.
- `chore`: Mantenimiento, dependencias o tareas de configuración.

#### Reglas Clave de los Commits
- **Idioma:** Todos los mensajes deben estar escritos obligatoriamente en español.
- **Actualización Atómica del Backlog:** El marcado de tareas completadas (en `tasks.md`, `backend_tasks.md`, `frontend_tasks.md`, etc.) debe incluirse dentro del mismo commit de la funcionalidad, evitando commits aislados para actualizar listas de tareas.
- **Docs-as-Code:** Toda modificación que altere endpoints, modelos de datos, variables de entorno o flujos de negocio debe incluir la actualización de la carpeta `/docs/` en el mismo commit.
