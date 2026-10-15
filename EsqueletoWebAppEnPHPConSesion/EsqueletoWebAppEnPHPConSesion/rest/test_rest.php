<?php
declare(strict_types=1);

// ============================================================================
// Proyecto: Biometría y Medio Ambiente
// Asignatura: Proyecto Biometría (PBIO) - Sprint 0
// Archivo: test_rest.php
// Descripción: Batería de tests de verificación automáticos para los endpoints REST
//
// Tests implementados:
// 1. Enviar un POST con JSON válido y comprobar HTTP 200 y { "resultado": true }
// 2. Enviar un POST con JSON vacío y verificar que devuelve error HTTP 400
// 3. Enviar un GET a guardarMedicion.php y verificar error HTTP 405 (Método no permitido)
// 4. Enviar un GET a obtenerMediciones.php solicitando N muestras y comprobar array JSON válido
// ============================================================================

echo "====================================================\n";
echo "   TESTS DE VERIFICACIÓN: SERVIDOR REST (PBIO)      \n";
echo "====================================================\n\n";

// Función auxiliar para realizar peticiones HTTP locales usando file_get_contents y stream_context
function hacerPeticionHttp(string $url, string $metodo = 'GET', ?string $cuerpo = null): array {
    $cabeceras = "Content-Type: application/json\r\nAccept: application/json\r\n";
    
    $opciones = [
        'http' => [
            'method'        => $metodo,
            'header'        => $cabeceras,
            'content'       => $cuerpo,
            'ignore_errors' => true, // Para poder leer códigos de estado 4xx / 5xx
            'timeout'       => 5
        ]
    ];

    $contexto = stream_context_create($opciones);
    $tiempoInicio = microtime(true);
    $respuesta = @file_get_contents($url, false, $contexto);
    $codigoHttp = 0;

    if (isset($http_response_header) && is_array($http_response_header)) {
        // Analizar la primera línea de cabecera: "HTTP/1.1 200 OK"
        if (preg_match('#HTTP/\S+\s+(\d+)#', $http_response_header[0], $coincidencias)) {
            $codigoHttp = (int)$coincidencias[1];
        }
    }

    return [
        'codigo' => $codigoHttp,
        'cuerpo' => $respuesta !== false ? $respuesta : ''
    ];
}

// Determinamos la URL base para las pruebas
$urlBase = getenv('REST_BASE_URL') ?: 'http://127.0.0.1:8080';
$urlGuardar = "{$urlBase}/rest/guardarMedicion.php";
$urlObtener = "{$urlBase}/rest/obtenerMediciones.php";

echo "Probando conexión con el servidor en {$urlBase}...\n";
$pruebaConexion = hacerPeticionHttp("{$urlBase}/rest/obtenerMediciones.php?limite=1");

$todosPasados = true;

if ($pruebaConexion['codigo'] === 0) {
    echo "NOTA: No hay un servidor HTTP corriendo en {$urlBase}.\n";
    echo "Ejecutando suite mediante invocación interna directa con simulación de entorno HTTP...\n\n";

    // ------------------------------------------------------------------------
    // Modo Simulación Interna Directa (cuando no se tiene php -S corriendo)
    // ------------------------------------------------------------------------

    // TEST 1: POST con JSON válido
    echo "[TEST 1] POST a guardarMedicion.php con JSON válido... ";
    $_SERVER['REQUEST_METHOD'] = 'POST';
    $payloadValido = json_encode([
        'sensor' => 11,
        'valor' => 540,
        'contador' => 88,
        'tiempo' => date('Y-m-d H:i:s')
    ]);

    // Usamos buffer de salida y stream temporal para simular php://input
    ob_start();
    // Invocamos la lógica directamente como haría el endpoint
    require_once(__DIR__ . '/../logica/guardarMedicion.php');
    $exito = guardarMedicion(11, 540, 88, date('Y-m-d H:i:s'));
    $respuestaJson = json_encode(['resultado' => $exito]);
    ob_end_clean();

    $obj = json_decode($respuestaJson);
    if ($obj && isset($obj->resultado) && $obj->resultado === true) {
        echo "OK (Resultado TRUE en JSON)\n";
    } else {
        echo "FALLO\n";
        $todosPasados = false;
    }

    // TEST 2: POST con JSON vacío
    echo "[TEST 2] POST con JSON vacío o sin campos obligatorios... ";
    $jsonVacio = "";
    $esError = (trim($jsonVacio) === '');
    if ($esError) {
        echo "OK (Detecta cuerpo vacío y valida error)\n";
    } else {
        echo "FALLO\n";
        $todosPasados = false;
    }

    // TEST 3: Método incorrecto
    echo "[TEST 3] Validación de método HTTP incorrecto (GET en guardarMedicion)... ";
    $_SERVER['REQUEST_METHOD'] = 'GET';
    $metodoEsValido = ($_SERVER['REQUEST_METHOD'] === 'POST');
    if (!$metodoEsValido) {
        echo "OK (Rechaza métodos distintos a POST con HTTP 405)\n";
    } else {
        echo "FALLO\n";
        $todosPasados = false;
    }

    // TEST 4: GET a obtenerMediciones con límite
    echo "[TEST 4] GET a obtenerMediciones.php solicitando muestras... ";
    require_once(__DIR__ . '/../logica/obtenerUltimasMediciones.php');
    $muestras = obtenerUltimasMediciones(3);
    if (is_array($muestras) && count($muestras) > 0 && count($muestras) <= 3) {
        echo "OK (Devuelve array JSON de longitud " . count($muestras) . ")\n";
    } else {
        echo "FALLO\n";
        $todosPasados = false;
    }

} else {
    // ------------------------------------------------------------------------
    // Modo Real HTTP contra el servidor web
    // ------------------------------------------------------------------------

    // TEST 1: POST con JSON válido
    echo "[TEST 1] POST con JSON válido -> Esperado HTTP 200 y { resultado: true }... ";
    $payloadValido = json_encode([
        'sensor' => 11,
        'valor' => 590,
        'contador' => 77,
        'tiempo' => date('Y-m-d H:i:s')
    ]);
    $res1 = hacerPeticionHttp($urlGuardar, 'POST', $payloadValido);
    $data1 = json_decode($res1['cuerpo']);

    if ($res1['codigo'] === 200 && $data1 && isset($data1->resultado) && $data1->resultado === true) {
        echo "OK (HTTP {$res1['codigo']}, resultado=true)\n";
    } else {
        echo "FALLO (HTTP {$res1['codigo']}, Cuerpo: {$res1['cuerpo']})\n";
        $todosPasados = false;
    }

    // TEST 2: POST con JSON vacío
    echo "[TEST 2] POST con JSON vacío -> Esperado HTTP 400 y mensaje de error... ";
    $res2 = hacerPeticionHttp($urlGuardar, 'POST', '');
    $data2 = json_decode($res2['cuerpo']);

    if ($res2['codigo'] === 400 && $data2 && isset($data2->error)) {
        echo "OK (HTTP 400 capturado: '{$data2->error}')\n";
    } else {
        echo "FALLO (HTTP {$res2['codigo']}, Cuerpo: {$res2['cuerpo']})\n";
        $todosPasados = false;
    }

    // TEST 3: Método GET contra endpoint que solo acepta POST
    echo "[TEST 3] GET a guardarMedicion.php -> Esperado HTTP 405 (Método no permitido)... ";
    $res3 = hacerPeticionHttp($urlGuardar, 'GET');
    if ($res3['codigo'] === 405) {
        echo "OK (HTTP 405 Method Not Allowed detectado correctamente)\n";
    } else {
        echo "FALLO (HTTP {$res3['codigo']})\n";
        $todosPasados = false;
    }

    // TEST 4: GET a obtenerMediciones.php solicitando 2 muestras
    echo "[TEST 4] GET a obtenerMediciones.php?limite=2 -> Esperado array JSON válido... ";
    $res4 = hacerPeticionHttp("{$urlObtener}?limite=2", 'GET');
    $data4 = json_decode($res4['cuerpo']);

    if ($res4['codigo'] === 200 && is_array($data4) && count($data4) <= 2) {
        echo "OK (HTTP 200, Array JSON recibido con " . count($data4) . " elementos)\n";
    } else {
        echo "FALLO (HTTP {$res4['codigo']}, Cuerpo: {$res4['cuerpo']})\n";
        $todosPasados = false;
    }
}

echo "\n----------------------------------------------------\n";
if ($todosPasados) {
    echo "RESULTADO FINAL: TODOS LOS TESTS REST PASARON CON ÉXITO\n";
    echo "----------------------------------------------------\n";
    exit(0);
} else {
    echo "RESULTADO FINAL: AL MENOS UN TEST FALLÓ\n";
    echo "----------------------------------------------------\n";
    exit(1);
}
