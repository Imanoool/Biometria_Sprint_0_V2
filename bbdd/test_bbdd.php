<?php
declare(strict_types=1);

// ============================================================================
// Proyecto: Biometría y Medio Ambiente
// Asignatura: Proyecto Biometría (PBIO) - Sprint 0
// Archivo: test_bbdd.php
// Descripción: Script de verificación automática de la base de datos (Tests 1, 2, 3)
// ============================================================================

require_once(__DIR__ . '/../logica/ConexionBD.php');

echo "====================================================\n";
echo "   TEST DE VERIFICACIÓN DE BASE DE DATOS (PBIO)     \n";
echo "====================================================\n\n";

$pdo = ConexionBD::getConexion();
$todosCorrectos = true;

// ----------------------------------------------------------------------------
// Test 1: Inserción de prueba con valores simulados
// ----------------------------------------------------------------------------
echo "[TEST 1] Insertar medición con valores simulados... ";
try {
    $sensorSimulado = 11; // CO2
    $valorSimulado = 512;
    $contadorSimulado = 99;
    $tiempoSimulado = date('Y-m-d H:i:s');

    $sqlInsert = "INSERT INTO mediciones (sensor, valor, contador, tiempo) 
                  VALUES (:sensor, :valor, :contador, :tiempo)";
    $stmt = $pdo->prepare($sqlInsert);
    $res = $stmt->execute([
        ':sensor' => $sensorSimulado,
        ':valor' => $valorSimulado,
        ':contador' => $contadorSimulado,
        ':tiempo' => $tiempoSimulado
    ]);

    if ($res) {
        $idInsertada = (int)$pdo->lastInsertId();
        echo "OK (ID insertado: $idInsertada)\n";
    } else {
        echo "FALLO: No se pudo insertar\n";
        $todosCorrectos = false;
    }
} catch (PDOException $e) {
    echo "ERROR: " . $e->getMessage() . "\n";
    $todosCorrectos = false;
}

// ----------------------------------------------------------------------------
// Test 2: Consulta para obtener la última medición consultada
// ----------------------------------------------------------------------------
echo "[TEST 2] Consulta para obtener la última medición... ";
try {
    $sqlSelect = "SELECT id, sensor, valor, contador, tiempo 
                  FROM mediciones 
                  ORDER BY id DESC 
                  LIMIT 1";
    $stmt = $pdo->query($sqlSelect);
    $ultima = $stmt->fetch();

    if ($ultima && (int)$ultima['sensor'] === $sensorSimulado && (int)$ultima['valor'] === $valorSimulado) {
        echo "OK\n";
        echo "         -> ID: {$ultima['id']}, Sensor: {$ultima['sensor']}, Valor: {$ultima['valor']}, Contador: {$ultima['contador']}, Tiempo: {$ultima['tiempo']}\n";
    } else {
        echo "FALLO: El registro recuperado no coincide con el último insertado\n";
        $todosCorrectos = false;
    }
} catch (PDOException $e) {
    echo "ERROR: " . $e->getMessage() . "\n";
    $todosCorrectos = false;
}

// ----------------------------------------------------------------------------
// Test 3: Test de Integridad (Campos nulos deben ser rechazados)
// ----------------------------------------------------------------------------
echo "[TEST 3] Test de integridad (rechazo de campos NULL)... ";
try {
    $sqlNull = "INSERT INTO mediciones (sensor, valor, contador, tiempo) 
                VALUES (:sensor, :valor, :contador, :tiempo)";
    $stmt = $pdo->prepare($sqlNull);
    
    // Forzamos un valor NULL en 'valor', campo definido como NOT NULL
    $stmt->bindValue(':sensor', 11, PDO::PARAM_INT);
    $stmt->bindValue(':valor', null, PDO::PARAM_NULL);
    $stmt->bindValue(':contador', 100, PDO::PARAM_INT);
    $stmt->bindValue(':tiempo', date('Y-m-d H:i:s'), PDO::PARAM_STR);
    
    $stmt->execute();

    echo "FALLO: La base de datos permitió insertar un valor NULL en un campo NOT NULL\n";
    $todosCorrectos = false;
} catch (PDOException $e) {
    echo "OK (Excepción esperada de integridad capturada: restricción NOT NULL activa)\n";
}

echo "\n----------------------------------------------------\n";
if ($todosCorrectos) {
    echo "RESULTADO FINAL: TODOS LOS TESTS DE BBDD PASARON CON ÉXITO\n";
    echo "----------------------------------------------------\n";
    exit(0);
} else {
    echo "RESULTADO FINAL: AL MENOS UN TEST FALLÓ\n";
    echo "----------------------------------------------------\n";
    exit(1);
}

