<?php
declare(strict_types=1);

// ============================================================================
// Proyecto: Biometría y Medio Ambiente
// Asignatura: Proyecto Biometría (PBIO) - Sprint 0
// Archivo: test_logica.php
// Descripción: Batería de tests de verificación automáticos para la lógica de negocio
//
// Tests implementados:
// 1. Comprobar que se guarda la medición y verificar que el retorno es true / false
// 2. Comprobar el valor del último objeto guardado
// 3. Invocar y comprobar que el array no está vacío y tiene la estructura de medición
// 4. Si hay campos nulos o vacíos verificar que devuelve error (false)
// ============================================================================

require_once(__DIR__ . '/guardarMedicion.php');
require_once(__DIR__ . '/obtenerUltimasMediciones.php');
require_once(__DIR__ . '/obtenerUltimaMedicion.php');

echo "====================================================\n";
echo "   TESTS DE VERIFICACIÓN: LÓGICA DE NEGOCIO (PBIO)  \n";
echo "====================================================\n\n";

$todosPasados = true;

// ----------------------------------------------------------------------------
// Test 1: Comprobar que se guarda la medición y el retorno es booleano
// ----------------------------------------------------------------------------
echo "[TEST 1] Guardar medición y verificar retorno booleano (true/false)... ";
$tiempoTest = date('Y-m-d H:i:s');
$valorTest = 620; // ppm de CO2 simulado
$contadorTest = 15;
$sensorTest = 11;

$retornoGuardar = guardarMedicion($sensorTest, $valorTest, $contadorTest, $tiempoTest);

if ($retornoGuardar === true) {
    echo "OK (Retornó TRUE estrictamente)\n";
} else {
    echo "FALLO (Esperado TRUE, se obtuvo FALSE)\n";
    $todosPasados = false;
}

// ----------------------------------------------------------------------------
// Test 2: Comprobar el valor que ha tenido el último objeto guardado
// ----------------------------------------------------------------------------
echo "[TEST 2] Comprobar el valor del último objeto guardado... ";
$ultimaMedicion = obtenerUltimaMedicion();

if ($ultimaMedicion !== null && 
    (int)$ultimaMedicion['valor'] === $valorTest && 
    (int)$ultimaMedicion['sensor'] === $sensorTest) {
    echo "OK (Valor obtenido: {$ultimaMedicion['valor']}, Sensor: {$ultimaMedicion['sensor']})\n";
} else {
    echo "FALLO (Los valores no coinciden con la última medición insertada)\n";
    $todosPasados = false;
}

// ----------------------------------------------------------------------------
// Test 3: Invocar y comprobar que el array no está vacío y tiene la estructura
// ----------------------------------------------------------------------------
echo "[TEST 3] Comprobar lista no vacía y estructura de cada medición... ";
$lista = obtenerUltimasMediciones(5);

if (is_array($lista) && count($lista) > 0) {
    $primera = $lista[0];
    $camposObligatorios = ['id', 'sensor', 'valor', 'contador', 'tiempo'];
    $estructuraValida = true;

    foreach ($camposObligatorios as $campo) {
        if (!array_key_exists($campo, $primera)) {
            $estructuraValida = false;
            break;
        }
    }

    if ($estructuraValida) {
        echo "OK (Total recuperadas: " . count($lista) . ", estructura con campos: " . implode(', ', $camposObligatorios) . ")\n";
    } else {
        echo "FALLO (Faltan campos en la estructura de medición)\n";
        $todosPasados = false;
    }
} else {
    echo "FALLO (El array está vacío o no es un array)\n";
    $todosPasados = false;
}

// ----------------------------------------------------------------------------
// Test 4: Comprobar que ante datos erróneos o campos vacíos devuelve error
// ----------------------------------------------------------------------------
echo "[TEST 4] Validación ante campos vacíos o no válidos... ";
$falloTiempoVacio = guardarMedicion(11, 400, 16, "   ");

if ($falloTiempoVacio === false) {
    echo "OK (Devuelve FALSE ante tiempo vacío)\n";
} else {
    echo "FALLO (Esperaba FALSE ante datos vacíos)\n";
    $todosPasados = false;
}

echo "\n----------------------------------------------------\n";
if ($todosPasados) {
    echo "RESULTADO FINAL: TODOS LOS TESTS DE LÓGICA PASARON CON ÉXITO\n";
    echo "----------------------------------------------------\n";
    exit(0);
} else {
    echo "RESULTADO FINAL: AL MENOS UN TEST FALLÓ\n";
    echo "----------------------------------------------------\n";
    exit(1);
}

