<?php
declare(strict_types=1);

require_once(__DIR__ . '/../logica/guardarMedicion.php');

// ----------------------------------------------------------------
//
// POST ../rest/guardarMedicion.php
//
// (sensor:Z, valor:Z, contador:N, tiempo:Texto) -> guardarMedicion() -> (resultado:Booleano) | error:Texto
//
// Cuerpo recibido: JSON { "sensor": Z, "valor": Z, "contador": N, "tiempo": Texto }
// Respuesta devuelta: JSON { "resultado": true } | { "error": Texto }
//
// ----------------------------------------------------------------

// Configurar cabeceras de respuesta HTTP
header('Content-Type: application/json; charset=utf-8');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type');

// Manejo de preflight CORS
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit;
}

// 1. Validar el método HTTP (debe ser estrictamente POST)
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405); // Method Not Allowed
    $respuesta = new stdClass();
    $respuesta->error = "Método no permitido. Se requiere POST";
    echo json_encode($respuesta);
    exit;
}

// 2. Leer y decodificar el cuerpo JSON de la petición
$cuerpoPeticion = file_get_contents('php://input');

if ($cuerpoPeticion === false || trim($cuerpoPeticion) === '') {
    http_response_code(400); // Bad Request
    $respuesta = new stdClass();
    $respuesta->error = "Cuerpo de la petición vacío";
    echo json_encode($respuesta);
    exit;
}

$datos = json_decode($cuerpoPeticion, true);

if (!is_array($datos)) {
    http_response_code(400);
    $respuesta = new stdClass();
    $respuesta->error = "JSON malformado o no válido";
    echo json_encode($respuesta);
    exit;
}

// 3. Validar la presencia de los campos obligatorios
if (!isset($datos['sensor']) || !isset($datos['valor']) || !isset($datos['contador'])) {
    http_response_code(400);
    $respuesta = new stdClass();
    $respuesta->error = "Faltan campos obligatorios (se requiere sensor, valor y contador)";
    echo json_encode($respuesta);
    exit;
}

// Asignar tiempo: si viene en el JSON se usa; si no, se marca el tiempo de llegada al servidor
$sensor = (int)$datos['sensor'];
$valor = (int)$datos['valor'];
$contador = (int)$datos['contador'];
$tiempo = isset($datos['tiempo']) && is_string($datos['tiempo']) && trim($datos['tiempo']) !== '' 
          ? trim($datos['tiempo']) 
          : date('Y-m-d H:i:s');

// 4. Invocar la función de la capa de lógica
$exito = guardarMedicion($sensor, $valor, $contador, $tiempo);

$respuesta = new stdClass();

if ($exito === true) {
    http_response_code(200); // OK
    $respuesta->resultado = true;
} else {
    http_response_code(500); // Internal Server Error
    $respuesta->resultado = false;
    $respuesta->error = "Error al almacenar la medición en la base de datos";
}

// 5. Serializar y devolver respuesta
echo json_encode($respuesta);

