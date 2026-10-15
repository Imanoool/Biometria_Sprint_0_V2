<?php
declare(strict_types=1);

// ============================================================================
// Proyecto: Biometría y Medio Ambiente
// Asignatura: Proyecto Biometría (PBIO) - Sprint 0
// Archivo: obtenerUltimaMedicion.php
// Capa: Lógica de negocio (3 capas)
//
// -------------------------------------------------
//
// sensor:Z -> obtenerUltimaMedicion() -> Medicion | null
//
// -------------------------------------------------
// ============================================================================

require_once(__DIR__ . '/ConexionBD.php');

/**
 * Obtiene la medición MÁS reciente guardada en la base de datos.
 * Opcionalmente puede filtrarse por un tipo de sensor específico.
 * No interactúa con HTTP ni formatea JSON.
 *
 * @param int|null $sensor Código opcional del sensor a filtrar
 * @return array|null Array asociativo con la medición más reciente o null si no existe
 */
function obtenerUltimaMedicion(?int $sensor = null): ?array {
    try {
        $pdo = ConexionBD::getConexion();

        if ($sensor !== null) {
            $sql = "SELECT id, sensor, valor, contador, tiempo 
                    FROM mediciones 
                    WHERE sensor = :sensor 
                    ORDER BY tiempo DESC, id DESC 
                    LIMIT 1";
            $stmt = $pdo->prepare($sql);
            $stmt->bindValue(':sensor', $sensor, PDO::PARAM_INT);
            $stmt->execute();
        } else {
            $sql = "SELECT id, sensor, valor, contador, tiempo 
                    FROM mediciones 
                    ORDER BY tiempo DESC, id DESC 
                    LIMIT 1";
            $stmt = $pdo->query($sql);
        }

        $medicion = $stmt->fetch();
        return $medicion !== false ? $medicion : null;
    } catch (PDOException $e) {
        error_log("Error en obtenerUltimaMedicion: " . $e->getMessage());
        return null;
    }
}

