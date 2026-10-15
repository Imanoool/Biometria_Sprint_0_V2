<?php
declare(strict_types=1);

// ============================================================================
// Proyecto: Biometría y Medio Ambiente
// Asignatura: Proyecto Biometría (PBIO) - Sprint 0
// Archivo: obtenerUltimasMediciones.php
// Capa: Lógica de negocio (3 capas)
//
// -------------------------------------------------
//
// limite:N -> obtenerUltimasMediciones() -> Array<Medicion>
//
// -------------------------------------------------
// ============================================================================

require_once(__DIR__ . '/ConexionBD.php');

/**
 * Obtiene una lista con las últimas mediciones guardadas en la base de datos,
 * ordenadas cronológicamente de forma descendente por el campo "tiempo".
 * No interactúa con HTTP ni maneja JSON.
 *
 * @param int|null $limite Cantidad máxima de mediciones a recuperar (opcional)
 * @return array Lista de mediciones (cada una como array asociativo con id, sensor, valor, contador, tiempo)
 */
function obtenerUltimasMediciones(?int $limite = null): array {
    try {
        $pdo = ConexionBD::getConexion();

        $sql = "SELECT id, sensor, valor, contador, tiempo 
                FROM mediciones 
                ORDER BY tiempo DESC, id DESC";

        if ($limite !== null && $limite > 0) {
            $sql .= " LIMIT :limite";
            $stmt = $pdo->prepare($sql);
            $stmt->bindValue(':limite', $limite, PDO::PARAM_INT);
            $stmt->execute();
        } else {
            $stmt = $pdo->query($sql);
        }

        $mediciones = $stmt->fetchAll();
        return $mediciones !== false ? $mediciones : [];
    } catch (PDOException $e) {
        error_log("Error en obtenerUltimasMediciones: " . $e->getMessage());
        return [];
    }
}
