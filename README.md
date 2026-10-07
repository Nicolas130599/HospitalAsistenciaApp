# HospitalAsistenciaApp – APF2 (JSF + Bootstrap)

Sistema web de asistencia del personal del Hospital San Gabriel.
Curso Desarrollo Web Integrado (UTP) – Avance de Proyecto Final 2.

**Tecnologías:** Jakarta Faces (Mojarra 4.1) · CDI (Weld) · JDBC + patrón DAO · MySQL · Bootstrap 5 · Maven · Apache Tomcat 10.1/11.

## Cómo ejecutarlo

1. **Base de datos:** en MySQL Workbench abre `script_hospital.sql` y ejecútalo **completo**.
   Crea la base `hospital_asistencia` (mismo esquema del script unificado) y carga datos de prueba.
   Se puede ejecutar varias veces sin duplicar datos.
2. **Conexión:** revisa `src/main/resources/db.properties` (puerto, usuario y contraseña de tu MySQL).
3. **NetBeans:** clic derecho en el proyecto → *Clean and Build* (Maven descarga Mojarra y Weld) → *Run*.
4. Abre `http://localhost:8080/HospitalAsistenciaApp/` (ajusta el puerto si tu Tomcat usa otro).

**Usuarios de prueba:** `admin / 123456` (ADMINISTRADOR, puede eliminar) y `rrhh / rrhh123` (RRHH).

> Requiere Tomcat 10.1 o superior (espacio de nombres `jakarta.*`). No funciona en Tomcat 9.

## Funcionalidades

| Pantalla | Qué hace |
|---|---|
| `login.xhtml` | Inicio de sesión; `AuthFilter` protege el resto de páginas. |
| `dashboard.xhtml` | Indicadores (totales, hoy, permisos, personal activo), asistencias por estado y por turno, últimos registros. |
| `registro.xhtml` | Registro de asistencia por DNI. Autocompleta al colaborador existente (AJAX), calcula hora de entrada y estado (Puntual / Tardanza), permite registrar un permiso con documento adjunto. |
| `listado.xhtml` | `h:dataTable` con paginación y **filtros combinados**: colaborador, DNI/nombre, cargo, turno, estado, permiso, tipo de permiso y rango de fechas. Modal de detalle. El rol ADMINISTRADOR puede eliminar. |

### Filtros por colaborador
Todos los filtros se combinan con `AND` en una sola consulta SQL (`AsistenciaDAO.where`), y el conteo
usa los mismos `JOIN` y condiciones que el listado, por lo que la paginación es exacta.

## Estructura

```
com.utp.hospital
├── conexion/Conexion.java          JDBC, lee db.properties
├── model/                          Usuario, Personal, Turno, TipoPermiso, Asistencia,
│                                   FiltroAsistencia, SolicitudRegistro, Indicador (DTO)
├── dao/                            UsuarioDAO, PersonalDAO, CatalogoDAO, AsistenciaDAO, NegocioException
├── bean/                           LoginBean (Session), DashboardBean (Request),
│                                   AsistenciaBean (View), ListadoBean (View)
└── filter/AuthFilter.java          protege las páginas .xhtml
webapp
├── WEB-INF/web.xml, faces-config.xml, beans.xml, templates/plantilla.xhtml
├── META-INF/context.xml            registra el BeanManager de Weld en Tomcat
├── login.xhtml, dashboard.xhtml, registro.xhtml, listado.xhtml
└── resources/css/hospital.css, resources/img/logo_san_gabriel.jpeg
```

## Notas

- Las contraseñas del script están en texto plano (esquema original). `UsuarioDAO` también acepta un hash SHA-256 en la
  columna `password`, para migrar a contraseñas cifradas en el proyecto final.
- Los adjuntos se guardan en la carpeta `hospital_uploads` del usuario del sistema; en la base se guarda nombre y ruta.
- Bootstrap se carga desde CDN: hace falta conexión a internet al abrir la aplicación.
