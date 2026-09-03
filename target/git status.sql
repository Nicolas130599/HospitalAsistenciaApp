CREATE DATABASE IF NOT EXISTS HospitalAsistenciaApp
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE HospitalAsistenciaApp;
DROP TABLE IF EXISTS asistencia;
DROP TABLE IF EXISTS empleados;
DROP TABLE IF EXISTS turnos;
DROP TABLE IF EXISTS tipos_permiso;

CREATE TABLE turnos (
    id_turno INT AUTO_INCREMENT PRIMARY KEY,
    nombre_turno VARCHAR(50) NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL,
    estado VARCHAR(20) DEFAULT 'Activo'
);

INSERT INTO turnos
(nombre_turno, hora_inicio, hora_fin, estado)
VALUES
('Mañana', '06:00:00', '14:00:00', 'Activo'),
('Tarde', '14:00:00', '22:00:00', 'Activo'),
('Noche', '22:00:00', '06:00:00', 'Activo');

CREATE TABLE tipos_permiso (
    id_permiso INT AUTO_INCREMENT PRIMARY KEY,
    nombre_permiso VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255),
    estado VARCHAR(20) DEFAULT 'Activo'
);

INSERT INTO tipos_permiso
(nombre_permiso, descripcion, estado)
VALUES
(
    'Descanso Médico',
    'Permiso por motivos de salud',
    'Activo'
),
(
    'Vacaciones',
    'Permiso correspondiente al periodo vacacional',
    'Activo'
),
(
    'Permiso Personal',
    'Permiso por motivos personales',
    'Activo'
);

CREATE TABLE empleados (
    id_empleado INT AUTO_INCREMENT PRIMARY KEY,
    dni VARCHAR(20) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    cargo VARCHAR(100),
    id_turno INT,
    estado VARCHAR(20) DEFAULT 'Activo',

    FOREIGN KEY (id_turno)
        REFERENCES turnos(id_turno)
);

INSERT INTO empleados
(dni, nombre, cargo, id_turno, estado)
VALUES
(
    '74829102',
    'Carlos Mendoza',
    'Enfermero',
    1,
    'Activo'
),
(
    '70234567',
    'Maria Rodriguez',
    'Administradora',
    2,
    'Activo'
),
(
    '71563248',
    'Luis Ramirez',
    'Médico',
    3,
    'Activo'
),
(
    '76543210',
    'Ana Torres',
    'Recepcionista',
    1,
    'Activo'
),
(
    '70987654',
    'Pedro Castillo',
    'Técnico de Enfermería',
    2,
    'Activo'
);

CREATE TABLE asistencia (

    id INT AUTO_INCREMENT PRIMARY KEY,

    dni VARCHAR(20) NOT NULL,

    nombre VARCHAR(100) NOT NULL,

    turno VARCHAR(50) NOT NULL,

    permiso_solicitado VARCHAR(5)
        DEFAULT 'No',

    tipo_permiso VARCHAR(100),

    dias_permiso INT
        DEFAULT 0,

    archivo_adjunto VARCHAR(255),

    observaciones VARCHAR(500),

    fecha_registro TIMESTAMP
        DEFAULT CURRENT_TIMESTAMP,

    estado VARCHAR(30)
        DEFAULT 'Registrado'
);

INSERT INTO asistencia
(
    dni,
    nombre,
    turno,
    permiso_solicitado,
    tipo_permiso,
    dias_permiso,
    archivo_adjunto,
    observaciones,
    estado
)
VALUES
(
    '74829102',
    'Carlos Mendoza',
    'Mañana',
    'No',
    NULL,
    0,
    NULL,
    'Asistencia normal',
    'Registrado'
);


INSERT INTO asistencia
(
    dni,
    nombre,
    turno,
    permiso_solicitado,
    tipo_permiso,
    dias_permiso,
    archivo_adjunto,
    observaciones,
    estado
)
VALUES
(
    '70234567',
    'Maria Rodriguez',
    'Tarde',
    'Si',
    'Descanso Médico',
    2,
    'descanso_medico.pdf',
    'Permiso médico presentado',
    'Pendiente'
);


INSERT INTO asistencia
(
    dni,
    nombre,
    turno,
    permiso_solicitado,
    tipo_permiso,
    dias_permiso,
    archivo_adjunto,
    observaciones,
    estado
)
VALUES
(
    '71563248',
    'Luis Ramirez',
    'Noche',
    'No',
    NULL,
    0,
    NULL,
    'Turno completado',
    'Registrado'
);


INSERT INTO asistencia
(
    dni,
    nombre,
    turno,
    permiso_solicitado,
    tipo_permiso,
    dias_permiso,
    archivo_adjunto,
    observaciones,
    estado
)
VALUES
(
    '76543210',
    'Ana Torres',
    'Mañana',
    'Si',
    'Vacaciones',
    5,
    'vacaciones.pdf',
    'Solicitud de vacaciones',
    'Pendiente'
);


INSERT INTO asistencia
(
    dni,
    nombre,
    turno,
    permiso_solicitado,
    tipo_permiso,
    dias_permiso,
    archivo_adjunto,
    observaciones,
    estado
)
VALUES
(
    '70987654',
    'Pedro Castillo',
    'Tarde',
    'No',
    NULL,
    0,
    NULL,
    'Asistencia registrada correctamente',
    'Registrado'
);

SELECT *
FROM asistencia
ORDER BY id DESC;

SELECT
    e.id_empleado,
    e.dni,
    e.nombre,
    e.cargo,
    t.nombre_turno,
    t.hora_inicio,
    t.hora_fin,
    e.estado
FROM empleados e
INNER JOIN turnos t
    ON e.id_turno = t.id_turno;

SELECT
    id,
    dni,
    nombre,
    turno,
    tipo_permiso,
    dias_permiso,
    fecha_registro,
    estado
FROM asistencia
WHERE permiso_solicitado = 'Si';

SELECT
    id,
    dni,
    nombre,
    turno,
    fecha_registro,
    estado
FROM asistencia
WHERE permiso_solicitado = 'No';

SELECT
    COUNT(*) AS total_registros
FROM asistencia;

SELECT
    COUNT(*) AS total_permisos
FROM asistencia
WHERE permiso_solicitado = 'Si';

SELECT
    turno,
    COUNT(*) AS cantidad
FROM asistencia
GROUP BY turno
ORDER BY cantidad DESC;

SELECT *
FROM asistencia
WHERE estado = 'Pendiente';

UPDATE asistencia
SET estado = 'Aprobado'
WHERE id = 2;

SELECT
    id,
    dni,
    nombre,
    turno,
    permiso_solicitado,
    tipo_permiso,
    dias_permiso,
    estado
FROM asistencia
ORDER BY id DESC;
