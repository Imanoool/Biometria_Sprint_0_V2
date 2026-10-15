// ============================================================================
// Proyecto: Biometría y Medio Ambiente
// Asignatura: Proyecto Biometría (PBIO) - Sprint 0
// Archivo: test_logicaFake.js
// Descripción: Suite de tests automáticos para la capa de abstracción logicaFake
//
// Tests implementados:
// 1. Carga automática de la batería de pruebas
// 2. Comprobar en consola y validar que la última medición no es nula y contiene propiedades
// 3. Invocar múltiples mediciones con límite (ej: límite 4) y comprobar tamaño de respuesta
// 4. Mostrar por pantalla los datos y certificar que todos los tests fueron pasados con éxito
// ============================================================================

function ejecutarTestsLogicaFake() {
    var contenedorResultados = document.getElementById('resultados-tests');
    var badgeEstado = document.getElementById('badge-estado');
    
    function registrarResultado(titulo, exito, detalle) {
        console.log((exito ? '✅ [PASS] ' : '❌ [FAIL] ') + titulo + (detalle ? ' -> ' + detalle : ''));
        
        if (contenedorResultados) {
            var item = document.createElement('div');
            item.className = 'test-item ' + (exito ? 'test-pass' : 'test-fail');
            item.innerHTML = '<strong>' + (exito ? '✔ ' : '✖ ') + titulo + '</strong>' +
                             (detalle ? '<p class="test-detalle">' + detalle + '</p>' : '');
            contenedorResultados.appendChild(item);
        }
    }

    var totalTests = 4;
    var testsPasados = 0;

    // Test 1: Carga del script y disponibilidad de funciones
    console.log('--- Iniciando Suite de Tests de logicaFake ---');
    if (typeof obtenerUltimaMedicion === 'function' && typeof obtenerUltimasMediciones === 'function') {
        registrarResultado('Test 1: Script cargado con éxito y funciones disponibles', true, 'obtenerUltimaMedicion y obtenerUltimasMediciones están definidas');
        testsPasados++;
    } else {
        registrarResultado('Test 1: Script cargado', false, 'Faltan funciones en el ámbito global');
    }

    // Primero insertamos una medición simulada para garantizar datos frescos en el test
    guardarMedicion({ sensor: 11, valor: 495, contador: 101 }, function(errGuardar, resGuardar) {
        
        // Test 2: Obtener última medición y verificar que no es nula y tiene las propiedades
        obtenerUltimaMedicion(function(err, medicion) {
            if (!err && medicion !== null && typeof medicion === 'object') {
                var tienePropiedades = ('sensor' in medicion) && ('valor' in medicion) && 
                                       ('contador' in medicion) && ('tiempo' in medicion);
                if (tienePropiedades) {
                    console.log('Última medición recibida:', medicion);
                    registrarResultado('Test 2: Última medición válida y con propiedades', true,
                        'ID: ' + medicion.id + ', Sensor: ' + medicion.sensor + ', Valor: ' + medicion.valor + 
                        ', Contador: ' + medicion.contador + ', Tiempo: ' + medicion.tiempo);
                    testsPasados++;
                } else {
                    registrarResultado('Test 2: Última medición', false, 'Faltan propiedades en el objeto recibido');
                }
            } else {
                registrarResultado('Test 2: Última medición', false, err || 'La medición recibida es nula');
            }

            // Test 3: Invocar múltiples mediciones con límite 4
            var limiteTest = 4;
            obtenerUltimasMediciones(limiteTest, function(errLista, lista) {
                if (!errLista && Array.isArray(lista)) {
                    var cumpleLimite = (lista.length <= limiteTest && lista.length > 0);
                    if (cumpleLimite) {
                        console.log('Array de mediciones recuperadas (límite ' + limiteTest + '):', lista);
                        registrarResultado('Test 3: Múltiples mediciones con límite ' + limiteTest, true,
                            'Recuperadas ' + lista.length + ' mediciones (menor o igual a ' + limiteTest + ')');
                        testsPasados++;
                    } else {
                        registrarResultado('Test 3: Múltiples mediciones', false,
                            'Longitud no esperada: ' + lista.length + ' (debe ser <= ' + limiteTest + ')');
                    }
                } else {
                    registrarResultado('Test 3: Múltiples mediciones', false, errLista || 'No se recibió un array');
                }

                // Test 4: Certificación en pantalla y consola
                if (testsPasados === (totalTests - 1)) {
                    testsPasados++;
                    registrarResultado('Test 4: Datos mostrados en pantalla y todos los tests pasados con éxito', true,
                        'Se han superado satisfactoriamente los 4 tests de verificación.');
                    
                    if (badgeEstado) {
                        badgeEstado.className = 'badge-exito';
                        badgeEstado.textContent = 'TODOS LOS TESTS PASADOS CON ÉXITO (' + testsPasados + '/' + totalTests + ')';
                    }
                } else {
                    registrarResultado('Test 4: Verificación global', false,
                        'Solo se completaron ' + testsPasados + ' de ' + totalTests + ' tests.');
                    if (badgeEstado) {
                        badgeEstado.className = 'badge-fallo';
                        badgeEstado.textContent = 'ALGUNOS TESTS FALLARON (' + testsPasados + '/' + totalTests + ')';
                    }
                }
            });
        });
    });
}

// Iniciar automáticamente cuando el DOM esté listo
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', ejecutarTestsLogicaFake);
} else {
    ejecutarTestsLogicaFake();
}
