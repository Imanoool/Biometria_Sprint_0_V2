<?php
declare(strict_types=1);

// ============================================================================
// Proyecto: Biometría y Medio Ambiente
// Asignatura: Proyecto Biometría (PBIO) - Sprint 0
// Archivo: guardarMedicion.php
// Capa: Lógica de negocio (3 capas)
//
// -------------------------------------------------
//
// (sensor:Z, valor:Z, contador:N, tiempo:Texto) -> guardarMedicion() -> Booleano
//
// -------------------------------------------------
// ============================================================================

require_once(__DIR__ . '/ConexionBD.php');

/**
 * Guarda una nueva lectura de medición en la base de datos.
 * Esta función pertenece estrictamente a la capa de lógica de negocio:
 * NO interactúa con HTTP ni formatea respuestas en JSON.
 *
 * @param int $sensor Código del sensor de gas (Z)
 * @param int $valor Magnitud leída (Z)
 * @param int $contador Número de secuencia de Arduino (N)
 * @param string $tiempo Marca de tiempo en el servidor (Texto)
 * @return bool TRUE si se guardó con éxito, FALSE si hubo un error o datos no válidos
 */
function guardarMedicion(int $sensor, int $valor, int $contador, string $tiempo): bool {
    // Validación estricta de campos obligatorios
    if (trim($tiempo) === '') {
        return false;
    }

    try {
        $pdo = ConexionBD::getConexion();

        $sql = "INSERT INTO mediciones (sensor, valor, contador, tiempo) 
                VALUES (:sensor, :valor, :contador, :tiempo)";
        
        $stmt = $pdo->prepare($sql);
        $resultado = $stmt->execute([
            ':sensor'   => $sensor,
            ':valor'    => $valor,
            ':contador' => $contador,
            ':tiempo'   => $tiempo
        ]);

        return $resultado && $stmt->rowCount() > 0;
    } catch (PDOException $e) {
        // En caso de violación de restricción NOT NULL, clave o fallo en BD
        error_log("Error en guardarMedicion: " . $e->getMessage());
        return false;
    }
}
