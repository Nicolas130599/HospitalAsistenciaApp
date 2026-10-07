-- =====================================================================
-- HOSPITAL ASISTENCIA APP - Script de base de datos (APF2)
-- Esquema: el mismo del script unificado (usuario, turno, personal, asistencia,
-- tipo_permiso, permiso, documento + vistas vista_asistencias y vista_dashboard).
-- Ejecutar COMPLETO en MySQL Workbench. Se puede ejecutar varias veces sin duplicar datos.
-- Incluye datos de prueba (personal y asistencias de los últimos 7 días) para el listado y el dashboard.
-- =====================================================================
SET NAMES utf8mb4;

CREATE DATABASE IF NOT EXISTS hospital_asistencia CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE hospital_asistencia;

-- ---------------------------------------------------------------------
-- Tablas
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS usuario (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    rol VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS turno (
    id_turno INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL
);

CREATE TABLE IF NOT EXISTS personal (
    id_personal INT AUTO_INCREMENT PRIMARY KEY,
    dni VARCHAR(8) NOT NULL UNIQUE,
    nombre VARCHAR(150) NOT NULL,
    cargo VARCHAR(100) NOT NULL,
    id_turno INT,
    activo BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (id_turno) REFERENCES turno(id_turno)
);

CREATE TABLE IF NOT EXISTS asistencia (
    id_asistencia INT AUTO_INCREMENT PRIMARY KEY,
    id_personal INT NOT NULL,
    fecha DATE NOT NULL,
    hora_entrada TIME NOT NULL,
    hora_salida TIME,
    estado VARCHAR(50) NOT NULL,
    observacion TEXT,
    FOREIGN KEY (id_personal) REFERENCES personal(id_personal) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS tipo_permiso (
    id_tipo INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT
);

-- Permiso con relación directa a la asistencia que lo originó (id_asistencia)
CREATE TABLE IF NOT EXISTS permiso (
    id_permiso INT AUTO_INCREMENT PRIMARY KEY,
    id_personal INT NOT NULL,
    id_tipo INT NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    dias_totales INT DEFAULT 1,
    motivo TEXT,
    estado VARCHAR(50) DEFAULT 'APROBADO',
    id_asistencia INT NULL,
    FOREIGN KEY (id_personal) REFERENCES personal(id_personal) ON DELETE CASCADE,
    FOREIGN KEY (id_tipo) REFERENCES tipo_permiso(id_tipo),
    FOREIGN KEY (id_asistencia) REFERENCES asistencia(id_asistencia) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS documento (
    id_documento INT AUTO_INCREMENT PRIMARY KEY,
    id_permiso INT NOT NULL,
    nombre_archivo VARCHAR(255) NOT NULL,
    ruta_archivo VARCHAR(255) NOT NULL,
    FOREIGN KEY (id_permiso) REFERENCES permiso(id_permiso) ON DELETE CASCADE
);

-- ---------------------------------------------------------------------
-- Datos base (no se duplican si el script se vuelve a ejecutar)
-- ---------------------------------------------------------------------
-- Usuarios: admin / 123456 (puede eliminar) y rrhh / rrhh123 (registra y consulta)
INSERT IGNORE INTO usuario (username, password, rol) VALUES
('admin', '123456', 'ADMINISTRADOR'),
('rrhh', 'rrhh123', 'RRHH');

INSERT INTO turno (nombre, hora_inicio, hora_fin)
SELECT v.nombre, v.hora_inicio, v.hora_fin FROM (
    SELECT 'Mañana' AS nombre, '08:00:00' AS hora_inicio, '16:00:00' AS hora_fin
    UNION ALL SELECT 'Tarde', '14:00:00', '22:00:00'
    UNION ALL SELECT 'Noche', '22:00:00', '06:00:00'
) v
WHERE NOT EXISTS (SELECT 1 FROM turno t WHERE t.nombre = v.nombre);

INSERT INTO tipo_permiso (nombre, descripcion)
SELECT v.nombre, v.descripcion FROM (
    SELECT 'Descanso médico' AS nombre, 'Permiso justificado por motivos de salud con certificado' AS descripcion
    UNION ALL SELECT 'Vacaciones', 'Permiso por días de descanso vacacional programado'
    UNION ALL SELECT 'Permiso personal', 'Permiso por asuntos particulares del colaborador'
    UNION ALL SELECT 'Comisión de servicios', 'Permiso por desplazamiento laboral oficial'
) v
WHERE NOT EXISTS (SELECT 1 FROM tipo_permiso t WHERE t.nombre = v.nombre);

-- ---------------------------------------------------------------------
-- Vistas (se recrean en cada ejecución)
-- ---------------------------------------------------------------------
DROP VIEW IF EXISTS vista_dashboard;
DROP VIEW IF EXISTS vista_asistencias;

CREATE VIEW vista_asistencias AS
SELECT
    a.id_asistencia,
    per.dni,
    per.nombre,
    t.nombre AS turno,
    a.fecha,
    a.hora_entrada,
    a.hora_salida,
    a.estado,
    IF(p.id_permiso IS NOT NULL, 'Sí', 'No') AS permiso_estado,
    COALESCE(tp.nombre, 'Sin permiso') AS tipo_permiso,
    COALESCE(doc.nombre_archivo, '-') AS documento_adjunto,
    COALESCE(p.dias_totales, 0) AS dias,
    COALESCE(p.motivo, a.observacion) AS observacion
FROM asistencia a
JOIN personal per ON a.id_personal = per.id_personal
LEFT JOIN turno t ON per.id_turno = t.id_turno
LEFT JOIN permiso p ON p.id_asistencia = a.id_asistencia
LEFT JOIN tipo_permiso tp ON p.id_tipo = tp.id_tipo
LEFT JOIN documento doc ON doc.id_permiso = p.id_permiso;

CREATE VIEW vista_dashboard AS
SELECT
    COUNT(*) AS total_hoy,
    COALESCE(SUM(CASE WHEN permiso_estado = 'Sí' THEN 1 ELSE 0 END), 0) AS total_permisos
FROM vista_asistencias
WHERE fecha = CURDATE();

-- ---------------------------------------------------------------------
-- Datos de prueba: personal (el DNI es único, INSERT IGNORE evita duplicados)
-- ---------------------------------------------------------------------
INSERT IGNORE INTO personal (dni, nombre, cargo, id_turno, activo) VALUES
('74829102', 'Dr. Carlos Mendoza', 'Médico', (SELECT id_turno FROM turno WHERE nombre = 'Mañana' ORDER BY id_turno LIMIT 1), TRUE),
('45128933', 'Enf. Lucía Paredes', 'Enfermera', (SELECT id_turno FROM turno WHERE nombre = 'Mañana' ORDER BY id_turno LIMIT 1), TRUE),
('70215584', 'Téc. Jorge Valdivia', 'Técnico', (SELECT id_turno FROM turno WHERE nombre = 'Tarde' ORDER BY id_turno LIMIT 1), TRUE),
('43987120', 'Dra. Marcela Quispe', 'Médico', (SELECT id_turno FROM turno WHERE nombre = 'Tarde' ORDER BY id_turno LIMIT 1), TRUE),
('72640918', 'Enf. Rosa Huamán', 'Enfermera', (SELECT id_turno FROM turno WHERE nombre = 'Noche' ORDER BY id_turno LIMIT 1), TRUE),
('46731205', 'Dr. Andrés Salazar', 'Médico', (SELECT id_turno FROM turno WHERE nombre = 'Noche' ORDER BY id_turno LIMIT 1), TRUE),
('71349826', 'Aux. Patricia Rojas', 'Auxiliar', (SELECT id_turno FROM turno WHERE nombre = 'Mañana' ORDER BY id_turno LIMIT 1), TRUE),
('44582071', 'Téc. Miguel Torres', 'Técnico', (SELECT id_turno FROM turno WHERE nombre = 'Tarde' ORDER BY id_turno LIMIT 1), TRUE),
('73016549', 'Dra. Elena Campos', 'Médico', (SELECT id_turno FROM turno WHERE nombre = 'Mañana' ORDER BY id_turno LIMIT 1), TRUE);

-- ---------------------------------------------------------------------
-- Datos de prueba: asistencias de los últimos 7 días (una por colaborador y día)
-- ---------------------------------------------------------------------
INSERT INTO asistencia (id_personal, fecha, hora_entrada, hora_salida, estado, observacion)
SELECT x.id_personal, x.fecha, x.hora_entrada, x.hora_salida, x.estado, x.observacion FROM (
    SELECT (SELECT id_personal FROM personal WHERE dni = '74829102') AS id_personal, CURDATE() - INTERVAL 0 DAY AS fecha, '07:52:00' AS hora_entrada, NULL AS hora_salida, 'Puntual' AS estado, NULL AS observacion
    UNION ALL
    SELECT (SELECT id_personal FROM personal WHERE dni = '45128933') AS id_personal, CURDATE() - INTERVAL 0 DAY AS fecha, '07:59:00' AS hora_entrada, NULL AS hora_salida, 'Puntual' AS estado, NULL AS observacion
    UNION ALL
    SELECT (SELECT id_personal FROM personal WHERE dni = '70215584') AS id_personal, CURDATE() - INTERVAL 0 DAY AS fecha, '13:52:00' AS hora_entrada, NULL AS hora_salida, 'Puntual' AS estado, NULL AS observacion
    UNION ALL
    SELECT (SELECT id_personal FROM personal WHERE dni = '71349826') AS id_personal, CURDATE() - INTERVAL 0 DAY AS fecha, '08:18:00' AS hora_entrada, NULL AS hora_salida, 'Tardanza' AS estado, 'Llegó 18 minutos después del inicio del turno' AS observacion
    UNION ALL
    SELECT (SELECT id_personal FROM personal WHERE dni = '74829102') AS id_personal, CURDATE() - INTERVAL 1 DAY AS fecha, '07:55:00' AS hora_entrada, '16:01:00' AS hora_salida, 'Puntual' AS estado, NULL AS observacion
    UNION ALL
    SELECT (SELECT id_personal FROM personal WHERE dni = '45128933') AS id_personal, CURDATE() - INTERVAL 1 DAY AS fecha, '08:25:00' AS hora_entrada, '16:06:00' AS hora_salida, 'Tardanza' AS estado, 'Llegó 25 minutos después del inicio del turno' AS observacion
    UNION ALL
    SELECT (SELECT id_personal FROM personal WHERE dni = '70215584') AS id_personal, CURDATE() - INTERVAL 1 DAY AS fecha, '14:15:00' AS hora_entrada, '22:02:00' AS hora_salida, 'Tardanza' AS estado, 'Llegó 15 minutos después del inicio del turno' AS observacion
    UNION ALL
    SELECT (SELECT id_personal FROM personal WHERE dni = '43987120') AS id_personal, CURDATE() - INTERVAL 1 DAY AS fecha, '14:02:00' AS hora_entrada, '22:07:00' AS hora_salida, 'Puntual' AS estado, NULL AS observacion
    UNION ALL
    SELECT (SELECT id_personal FROM personal WHERE dni = '46731205') AS id_personal, CURDATE() - INTERVAL 1 DAY AS fecha, '22:02:00' AS hora_entrada, '06:08:00' AS hora_salida, 'Puntual' AS estado, NULL AS observacion
    UNION ALL
    SELECT (SELECT id_personal FROM personal WHERE dni = '71349826') AS id_personal, CURDATE() - INTERVAL 1 DAY AS fecha, '07:55:00' AS hora_entrada, '16:04:00' AS hora_salida, 'Puntual' AS estado, NULL AS observacion
    UNION ALL
    SELECT (SELECT id_personal FROM personal WHERE dni = '44582071') AS id_personal, CURDATE() - INTERVAL 1 DAY AS fecha, '14:02:00' AS hora_entrada, '22:00:00' AS hora_salida, 'Puntual' AS estado, NULL AS observacion
    UNION ALL
    SELECT (SELECT id_personal FROM personal WHERE dni = '73016549') AS id_personal, CURDATE() - INTERVAL 1 DAY AS fecha, '08:00:00' AS hora_entrada, NULL AS hora_salida, 'Permiso' AS estado, NULL AS observacion
    UNION ALL
    SELECT (SELECT id_personal FROM personal WHERE dni = '74829102') AS id_personal, CURDATE() - INTERVAL 2 DAY AS fecha, '08:32:00' AS hora_entrada, '16:02:00' AS hora_salida, 'Tardanza' AS estado, 'Llegó 32 minutos después del inicio del turno' AS observacion
    UNION ALL
    SELECT (SELECT id_personal FROM personal WHERE dni = '45128933') AS id_personal, CURDATE() - INTERVAL 2 DAY AS fecha, '08:05:00' AS hora_entrada, '16:07:00' AS hora_salida, 'Puntual' AS estado, NULL AS observacion
    UNION ALL
    SELECT (SELECT id_personal FROM personal WHERE dni = '70215584') AS id_personal, CURDATE() - INTERVAL 2 DAY AS fecha, '13:58:00' AS hora_entrada, '22:03:00' AS hora_salida, 'Puntual' AS estado, NULL AS observacion
    UNION ALL
    SELECT (SELECT id_personal FROM personal WHERE dni = '43987120') AS id_personal, CURDATE() - INTERVAL 2 DAY AS fecha, '14:00:00' AS hora_entrada, NULL AS hora_salida, 'Permiso' AS estado, NULL AS observacion
    UNION ALL
    SELECT (SELECT id_personal FROM personal WHERE dni = '72640918') AS id_personal, CURDATE() - INTERVAL 2 DAY AS fecha, '21:58:00' AS hora_entrada, '06:04:00' AS hora_salida, 'Puntual' AS estado, NULL AS observacion
    UNION ALL
    SELECT (SELECT id_personal FROM personal WHERE dni = '46731205') AS id_personal, CURDATE() - INTERVAL 2 DAY AS fecha, '22:05:00' AS hora_entrada, '06:00:00' AS hora_salida, 'Puntual' AS estado, NULL AS observacion
    UNION ALL
    SELECT (SELECT id_personal FROM personal WHERE dni = '71349826') AS id_personal, CURDATE() - INTERVAL 2 DAY AS fecha, '07:58:00' AS hora_entrada, '16:05:00' AS hora_salida, 'Puntual' AS estado, NULL AS observacion
    UNION ALL
    SELECT (SELECT id_personal FROM personal WHERE dni = '73016549') AS id_personal, CURDATE() - INTERVAL 2 DAY AS fecha, '07:58:00' AS hora_entrada, '16:06:00' AS hora_salida, 'Puntual' AS estado, NULL AS observacion
    UNION ALL
    SELECT (SELECT id_personal FROM personal WHERE dni = '74829102') AS id_personal, CURDATE() - INTERVAL 3 DAY AS fecha, '08:01:00' AS hora_entrada, '16:03:00' AS hora_salida, 'Puntual' AS estado, NULL AS observacion
    UNION ALL
    SELECT (SELECT id_personal FROM personal WHERE dni = '70215584') AS id_personal, CURDATE() - INTERVAL 3 DAY AS fecha, '14:01:00' AS hora_entrada, '22:04:00' AS hora_salida, 'Puntual' AS estado, NULL AS observacion
    UNION ALL
    SELECT (SELECT id_personal FROM personal WHERE dni = '43987120') AS id_personal, CURDATE() - INTERVAL 3 DAY AS fecha, '13:54:00' AS hora_entrada, '22:00:00' AS hora_salida, 'Puntual' AS estado, NULL AS observacion
    UNION ALL
    SELECT (SELECT id_personal FROM personal WHERE dni = '72640918') AS id_personal, CURDATE() - INTERVAL 3 DAY AS fecha, '22:01:00' AS hora_entrada, '06:05:00' AS hora_salida, 'Puntual' AS estado, NULL AS observacion
    UNION ALL
    SELECT (SELECT id_personal FROM personal WHERE dni = '46731205') AS id_personal, CURDATE() - INTERVAL 3 DAY AS fecha, '22:00:00' AS hora_entrada, NULL AS hora_salida, 'Permiso' AS estado, NULL AS observacion
    UNION ALL
    SELECT (SELECT id_personal FROM personal WHERE dni = '71349826') AS id_personal, CURDATE() - INTERVAL 3 DAY AS fecha, '08:01:00' AS hora_entrada, '16:06:00' AS hora_salida, 'Puntual' AS estado, NULL AS observacion
    UNION ALL
    SELECT (SELECT id_personal FROM personal WHERE dni = '44582071') AS id_personal, CURDATE() - INTERVAL 3 DAY AS fecha, '14:40:00' AS hora_entrada, '22:02:00' AS hora_salida, 'Tardanza' AS estado, 'Llegó 40 minutos después del inicio del turno' AS observacion
    UNION ALL
    SELECT (SELECT id_personal FROM personal WHERE dni = '73016549') AS id_personal, CURDATE() - INTERVAL 3 DAY AS fecha, '08:01:00' AS hora_entrada, '16:07:00' AS hora_salida, 'Puntual' AS estado, NULL AS observacion
    UNION ALL
    SELECT (SELECT id_personal FROM personal WHERE dni = '74829102') AS id_personal, CURDATE() - INTERVAL 4 DAY AS fecha, '08:04:00' AS hora_entrada, '16:04:00' AS hora_salida, 'Puntual' AS estado, NULL AS observacion
    UNION ALL
    SELECT (SELECT id_personal FROM personal WHERE dni = '45128933') AS id_personal, CURDATE() - INTERVAL 4 DAY AS fecha, '07:57:00' AS hora_entrada, '16:00:00' AS hora_salida, 'Puntual' AS estado, NULL AS observacion
    UNION ALL
    SELECT (SELECT id_personal FROM personal WHERE dni = '70215584') AS id_personal, CURDATE() - INTERVAL 4 DAY AS fecha, '14:00:00' AS hora_entrada, NULL AS hora_salida, 'Permiso' AS estado, NULL AS observacion
    UNION ALL
    SELECT (SELECT id_personal FROM personal WHERE dni = '43987120') AS id_personal, CURDATE() - INTERVAL 4 DAY AS fecha, '13:57:00' AS hora_entrada, '22:01:00' AS hora_salida, 'Puntual' AS estado, NULL AS observacion
    UNION ALL
    SELECT (SELECT id_personal FROM personal WHERE dni = '72640918') AS id_personal, CURDATE() - INTERVAL 4 DAY AS fecha, '22:22:00' AS hora_entrada, '06:06:00' AS hora_salida, 'Tardanza' AS estado, 'Llegó 22 minutos después del inicio del turno' AS observacion
    UNION ALL
    SELECT (SELECT id_personal FROM personal WHERE dni = '46731205') AS id_personal, CURDATE() - INTERVAL 4 DAY AS fecha, '21:57:00' AS hora_entrada, '06:02:00' AS hora_salida, 'Puntual' AS estado, NULL AS observacion
    UNION ALL
    SELECT (SELECT id_personal FROM personal WHERE dni = '71349826') AS id_personal, CURDATE() - INTERVAL 4 DAY AS fecha, '08:04:00' AS hora_entrada, '16:07:00' AS hora_salida, 'Puntual' AS estado, NULL AS observacion
    UNION ALL
    SELECT (SELECT id_personal FROM personal WHERE dni = '44582071') AS id_personal, CURDATE() - INTERVAL 4 DAY AS fecha, '13:57:00' AS hora_entrada, '22:03:00' AS hora_salida, 'Puntual' AS estado, NULL AS observacion
    UNION ALL
    SELECT (SELECT id_personal FROM personal WHERE dni = '73016549') AS id_personal, CURDATE() - INTERVAL 4 DAY AS fecha, '08:04:00' AS hora_entrada, '16:08:00' AS hora_salida, 'Puntual' AS estado, NULL AS observacion
    UNION ALL
    SELECT (SELECT id_personal FROM personal WHERE dni = '74829102') AS id_personal, CURDATE() - INTERVAL 5 DAY AS fecha, '07:53:00' AS hora_entrada, '16:05:00' AS hora_salida, 'Puntual' AS estado, NULL AS observacion
    UNION ALL
    SELECT (SELECT id_personal FROM personal WHERE dni = '45128933') AS id_personal, CURDATE() - INTERVAL 5 DAY AS fecha, '08:00:00' AS hora_entrada, '16:01:00' AS hora_salida, 'Puntual' AS estado, NULL AS observacion
    UNION ALL
    SELECT (SELECT id_personal FROM personal WHERE dni = '70215584') AS id_personal, CURDATE() - INTERVAL 5 DAY AS fecha, '13:53:00' AS hora_entrada, '22:06:00' AS hora_salida, 'Puntual' AS estado, NULL AS observacion
    UNION ALL
    SELECT (SELECT id_personal FROM personal WHERE dni = '43987120') AS id_personal, CURDATE() - INTERVAL 5 DAY AS fecha, '14:00:00' AS hora_entrada, '22:02:00' AS hora_salida, 'Puntual' AS estado, NULL AS observacion
    UNION ALL
    SELECT (SELECT id_personal FROM personal WHERE dni = '72640918') AS id_personal, CURDATE() - INTERVAL 5 DAY AS fecha, '21:53:00' AS hora_entrada, '06:07:00' AS hora_salida, 'Puntual' AS estado, NULL AS observacion
    UNION ALL
    SELECT (SELECT id_personal FROM personal WHERE dni = '46731205') AS id_personal, CURDATE() - INTERVAL 5 DAY AS fecha, '22:00:00' AS hora_entrada, '06:03:00' AS hora_salida, 'Puntual' AS estado, NULL AS observacion
    UNION ALL
    SELECT (SELECT id_personal FROM personal WHERE dni = '44582071') AS id_personal, CURDATE() - INTERVAL 5 DAY AS fecha, '14:00:00' AS hora_entrada, '22:04:00' AS hora_salida, 'Puntual' AS estado, NULL AS observacion
    UNION ALL
    SELECT (SELECT id_personal FROM personal WHERE dni = '73016549') AS id_personal, CURDATE() - INTERVAL 5 DAY AS fecha, '08:28:00' AS hora_entrada, '16:00:00' AS hora_salida, 'Tardanza' AS estado, 'Llegó 28 minutos después del inicio del turno' AS observacion
    UNION ALL
    SELECT (SELECT id_personal FROM personal WHERE dni = '45128933') AS id_personal, CURDATE() - INTERVAL 6 DAY AS fecha, '08:03:00' AS hora_entrada, '16:02:00' AS hora_salida, 'Puntual' AS estado, NULL AS observacion
    UNION ALL
    SELECT (SELECT id_personal FROM personal WHERE dni = '70215584') AS id_personal, CURDATE() - INTERVAL 6 DAY AS fecha, '13:56:00' AS hora_entrada, '22:07:00' AS hora_salida, 'Puntual' AS estado, NULL AS observacion
    UNION ALL
    SELECT (SELECT id_personal FROM personal WHERE dni = '43987120') AS id_personal, CURDATE() - INTERVAL 6 DAY AS fecha, '14:35:00' AS hora_entrada, '22:03:00' AS hora_salida, 'Tardanza' AS estado, 'Llegó 35 minutos después del inicio del turno' AS observacion
    UNION ALL
    SELECT (SELECT id_personal FROM personal WHERE dni = '72640918') AS id_personal, CURDATE() - INTERVAL 6 DAY AS fecha, '21:56:00' AS hora_entrada, '06:08:00' AS hora_salida, 'Puntual' AS estado, NULL AS observacion
    UNION ALL
    SELECT (SELECT id_personal FROM personal WHERE dni = '46731205') AS id_personal, CURDATE() - INTERVAL 6 DAY AS fecha, '22:03:00' AS hora_entrada, '06:04:00' AS hora_salida, 'Puntual' AS estado, NULL AS observacion
    UNION ALL
    SELECT (SELECT id_personal FROM personal WHERE dni = '71349826') AS id_personal, CURDATE() - INTERVAL 6 DAY AS fecha, '07:56:00' AS hora_entrada, '16:00:00' AS hora_salida, 'Puntual' AS estado, NULL AS observacion
    UNION ALL
    SELECT (SELECT id_personal FROM personal WHERE dni = '44582071') AS id_personal, CURDATE() - INTERVAL 6 DAY AS fecha, '14:03:00' AS hora_entrada, '22:05:00' AS hora_salida, 'Puntual' AS estado, NULL AS observacion
    UNION ALL
    SELECT (SELECT id_personal FROM personal WHERE dni = '73016549') AS id_personal, CURDATE() - INTERVAL 6 DAY AS fecha, '07:56:00' AS hora_entrada, '16:01:00' AS hora_salida, 'Puntual' AS estado, NULL AS observacion
) x
WHERE x.id_personal IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM asistencia a WHERE a.id_personal = x.id_personal AND a.fecha = x.fecha);

-- ---------------------------------------------------------------------
-- Datos de prueba: permisos y documentos adjuntos
-- ---------------------------------------------------------------------
INSERT INTO permiso (id_personal, id_tipo, fecha_inicio, fecha_fin, dias_totales, motivo, estado, id_asistencia)
SELECT a.id_personal, (SELECT id_tipo FROM tipo_permiso WHERE nombre = 'Descanso médico' ORDER BY id_tipo LIMIT 1),
       a.fecha, a.fecha + INTERVAL 2 DAY, 3, 'Reposo médico por cuadro gripal', 'APROBADO', a.id_asistencia
FROM asistencia a JOIN personal per ON per.id_personal = a.id_personal
WHERE per.dni = '43987120' AND a.fecha = CURDATE() - INTERVAL 2 DAY
  AND NOT EXISTS (SELECT 1 FROM permiso p WHERE p.id_asistencia = a.id_asistencia);

INSERT INTO documento (id_permiso, nombre_archivo, ruta_archivo)
SELECT p.id_permiso, 'certificado_medico_quispe.pdf', 'hospital_uploads/certificado_medico_quispe.pdf'
FROM permiso p JOIN asistencia a ON a.id_asistencia = p.id_asistencia
JOIN personal per ON per.id_personal = a.id_personal
WHERE per.dni = '43987120' AND a.fecha = CURDATE() - INTERVAL 2 DAY
  AND NOT EXISTS (SELECT 1 FROM documento d WHERE d.id_permiso = p.id_permiso);

INSERT INTO permiso (id_personal, id_tipo, fecha_inicio, fecha_fin, dias_totales, motivo, estado, id_asistencia)
SELECT a.id_personal, (SELECT id_tipo FROM tipo_permiso WHERE nombre = 'Vacaciones' ORDER BY id_tipo LIMIT 1),
       a.fecha, a.fecha + INTERVAL 6 DAY, 7, 'Vacaciones programadas del periodo', 'APROBADO', a.id_asistencia
FROM asistencia a JOIN personal per ON per.id_personal = a.id_personal
WHERE per.dni = '46731205' AND a.fecha = CURDATE() - INTERVAL 3 DAY
  AND NOT EXISTS (SELECT 1 FROM permiso p WHERE p.id_asistencia = a.id_asistencia);

INSERT INTO permiso (id_personal, id_tipo, fecha_inicio, fecha_fin, dias_totales, motivo, estado, id_asistencia)
SELECT a.id_personal, (SELECT id_tipo FROM tipo_permiso WHERE nombre = 'Permiso personal' ORDER BY id_tipo LIMIT 1),
       a.fecha, a.fecha + INTERVAL 0 DAY, 1, 'Trámite personal en entidad pública', 'APROBADO', a.id_asistencia
FROM asistencia a JOIN personal per ON per.id_personal = a.id_personal
WHERE per.dni = '73016549' AND a.fecha = CURDATE() - INTERVAL 1 DAY
  AND NOT EXISTS (SELECT 1 FROM permiso p WHERE p.id_asistencia = a.id_asistencia);

INSERT INTO permiso (id_personal, id_tipo, fecha_inicio, fecha_fin, dias_totales, motivo, estado, id_asistencia)
SELECT a.id_personal, (SELECT id_tipo FROM tipo_permiso WHERE nombre = 'Comisión de servicios' ORDER BY id_tipo LIMIT 1),
       a.fecha, a.fecha + INTERVAL 1 DAY, 2, 'Capacitación en otra sede hospitalaria', 'APROBADO', a.id_asistencia
FROM asistencia a JOIN personal per ON per.id_personal = a.id_personal
WHERE per.dni = '70215584' AND a.fecha = CURDATE() - INTERVAL 4 DAY
  AND NOT EXISTS (SELECT 1 FROM permiso p WHERE p.id_asistencia = a.id_asistencia);

INSERT INTO documento (id_permiso, nombre_archivo, ruta_archivo)
SELECT p.id_permiso, 'constancia_comision_valdivia.pdf', 'hospital_uploads/constancia_comision_valdivia.pdf'
FROM permiso p JOIN asistencia a ON a.id_asistencia = p.id_asistencia
JOIN personal per ON per.id_personal = a.id_personal
WHERE per.dni = '70215584' AND a.fecha = CURDATE() - INTERVAL 4 DAY
  AND NOT EXISTS (SELECT 1 FROM documento d WHERE d.id_permiso = p.id_permiso);

-- ---------------------------------------------------------------------
-- Verificación
-- ---------------------------------------------------------------------
SELECT 'personal' AS tabla, COUNT(*) AS filas FROM personal
UNION ALL SELECT 'asistencia', COUNT(*) FROM asistencia
UNION ALL SELECT 'permiso', COUNT(*) FROM permiso
UNION ALL SELECT 'documento', COUNT(*) FROM documento
UNION ALL SELECT 'usuario', COUNT(*) FROM usuario;
SELECT * FROM vista_asistencias ORDER BY fecha DESC, hora_entrada DESC LIMIT 10;
