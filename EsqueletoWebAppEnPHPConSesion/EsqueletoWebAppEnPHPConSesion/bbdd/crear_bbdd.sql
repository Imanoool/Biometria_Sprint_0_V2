-- ============================================================================
-- Proyecto: Biometría y Medio Ambiente
-- Asignatura: Proyecto Biometría (PBIO) - Sprint 0
-- Archivo: crear_bbdd.sql
-- Descripción: Creación de la base de datos y tabla para almacenar las mediciones
--              de gases emitidas por Arduino vía Bluetooth y reenviadas por REST.
--
-- Esquema Lógico:
-- Medición = (id: N, sensor: Z, valor: Z, contador: N, tiempo: Text)
--
-- Restricciones:
-- - Todos los campos son obligatorios (NOT NULL).
-- - 'id' es la clave primaria autoincremental.
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 1. Creación de la base de datos (para MySQL / MariaDB)
-- ----------------------------------------------------------------------------
CREATE DATABASE IF NOT EXISTS biometria_medioambiente
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE biometria_medioambiente;

-- ----------------------------------------------------------------------------
-- 2. Eliminación de tabla previa si existiese
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS mediciones;

-- ----------------------------------------------------------------------------
-- 3. Creación de la tabla 'mediciones'
-- ----------------------------------------------------------------------------
CREATE TABLE mediciones (
    -- id: N -> Clave primaria entera autoincremental
    id INT AUTO_INCREMENT PRIMARY KEY,
    
    -- sensor: Z -> Tipo o código de gas que se detecta (ej. 11 = CO2, 12 = Temperatura, 13 = Ruido)
    sensor INT NOT NULL,
    
    -- valor: Z -> Magnitud leída del sensor (ej. concentración en ppm)
    valor INT NOT NULL,
    
    -- contador: N -> Número de secuencia emitido por Arduino
    contador INT NOT NULL,
    
    -- tiempo: Text -> Marca temporal en la que fue recibido en el servidor
    tiempo VARCHAR(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Índices para optimizar búsquedas por sensor y orden cronológico
CREATE INDEX idx_sensor ON mediciones(sensor);
CREATE INDEX idx_tiempo ON mediciones(tiempo);

-- ----------------------------------------------------------------------------
-- 4. Inserción de datos iniciales simulados para verificación
-- ----------------------------------------------------------------------------
INSERT INTO mediciones (sensor, valor, contador, tiempo) VALUES
(11, 420, 1, '2026-10-15 20:00:00'),
(11, 435, 2, '2026-10-15 20:00:10'),
(12, 24,  3, '2026-10-15 20:00:20'),
(11, 450, 4, '2026-10-15 20:00:30'),
(13, 55,  5, '2026-10-15 20:00:40');
