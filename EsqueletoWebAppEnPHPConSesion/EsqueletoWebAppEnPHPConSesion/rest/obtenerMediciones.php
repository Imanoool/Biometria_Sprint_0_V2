<?php
declare(strict_types=1);

require_once(__DIR__ . '/../logica/obtenerUltimasMediciones.php');
require_once(__DIR__ . '/../logica/obtenerUltimaMedicion.php');

// ----------------------------------------------------------------
//
// GET ../rest/obtenerMediciones.php?sensor=<Z>&limite=<N>&ultima=<Booleano>
//
// (sensor:Z, limite:N) -> obtenerMediciones() -> Array<Medicion> | error:Texto
//
// Parámetros por query string:
// - sensor: (opcional) código de gas filtrado
// - limite: (opcional) número máximo de muestras a devolver
// - ultima: (opcional) si es "true", devuelve únicamente el último objeto o array con la última medición
//
// Devuelve: Array JSON con las mediciones consultadas o un objeto JSON de error
//
// ----------------------------------------------------------------

// Configurar cabeceras de respuesta HTTP
header('Content-Type: application/json; charset=utf-8');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type');

// Manejo de preflight CORS
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit;
}

// 1. Validar el método HTTP (debe ser estrictamente GET)
if ($_SERVER['REQUEST_METHOD'] !== 'GET') {
    http_response_code(405); // Method Not Allowed
    $respuesta = new stdClass();
    $respuesta->error = "Método no permitido. Se requiere GET";
    echo json_encode($respuesta);
    exit;
}

// 2. Extraer parámetros opcionales de la consulta
$sensor = isset($_GET['sensor']) && is_numeric($_GET['sensor']) ? (int)$_GET['sensor'] : null;
$limite = isset($_GET['limite']) && is_numeric($_GET['limite']) ? (int)$_GET['limite'] : null;
$esUltima = isset($_GET['ultima']) && ($_GET['ultima'] === 'true' || $_GET['ultima'] === '1');

// 3. Invocar la capa de lógica correspondiente
if ($esUltima) {
    // Si solicitan únicamente la última medición
    $medicion = obtenerUltimaMedicion($sensor);
    if ($medicion === null) {
        // Devolvemos array vacío o null
        echo json_encode([]);
    } else {
        // Para compatibilidad con clientes que esperan array o objeto único
        echo json_encode($medicion);
    }
} else {
    // Si solicitan lista de mediciones
    $mediciones = obtenerUltimasMediciones($limite);

    // Si se especificó filtro de sensor, filtramos las mediciones
    if ($sensor !== null) {
        $mediciones = array_values(array_filter($mediciones, function($m) use ($sensor) {
            return (int)$m['sensor'] === $sensor;
        }));
    }

    echo json_encode($mediciones);
}
