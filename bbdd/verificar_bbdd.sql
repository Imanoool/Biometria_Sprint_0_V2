-- ============================================================================
-- Proyecto: Biometría y Medio Ambiente
-- Asignatura: Proyecto Biometría (PBIO) - Sprint 0
-- Archivo: verificar_bbdd.sql
-- Descripción: Script de verificación de base de datos
--
-- Pruebas implementadas:
-- 1. Insertar una prueba con valores simulados
-- 2. Prueba de consulta para obtener la última medición consultada
-- 3. Test de Integridad para asegurar que valores nulos son rechazados
-- ============================================================================

USE biometria_medioambiente;

-- ----------------------------------------------------------------------------
-- Test 1: Inserción de prueba con valores simulados
-- ----------------------------------------------------------------------------
-- Simula la recepción de una trama de Arduino con sensor CO2 (11), valor 480 ppm, secuencia 6
INSERT INTO mediciones (sensor, valor, contador, tiempo)
VALUES (11, 480, 6, '2026-10-15 20:30:00');

-- ----------------------------------------------------------------------------
-- Test 2: Consulta para obtener la última medición consultada
-- ----------------------------------------------------------------------------
-- Devuelve la última medición registrada basándose en el identificador o tiempo
SELECT id, sensor, valor, contador, tiempo
FROM mediciones
ORDER BY id DESC
LIMIT 1;

-- ----------------------------------------------------------------------------
-- Test 3: Test de Integridad (NOT NULL)
-- ----------------------------------------------------------------------------
-- Esta sentencia DEBE fallar porque 'valor' es NOT NULL.
-- En un entorno transaccional o de pruebas automáticas comprueba que la BD
-- rechaza registros incompletos.
-- NOTA: Al ejecutarse provocará el error 1048: Column 'valor' cannot be null.
-- INSERT INTO mediciones (sensor, valor, contador, tiempo) VALUES (11, NULL, 7, '2026-10-15 20:30:10');

