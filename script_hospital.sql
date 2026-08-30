CREATE TABLE asistencia (
    id INT AUTO_INCREMENT PRIMARY KEY,
    dni VARCHAR(20) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    turno VARCHAR(50) NOT NULL,
    permiso_solicitado VARCHAR(5),
    tipo_permiso VARCHAR(50),
    dias_permiso INT,
    archivo_adjunto VARCHAR(255),
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
SELECT * FROM asistencia;
ALTER TABLE asistencia ADD COLUMN permiso_salud VARCHAR(5) DEFAULT 'No';
CREATE DATABASE IF NOT EXISTS HospitalAsistenciaApp;
USE HospitalAsistenciaApp;