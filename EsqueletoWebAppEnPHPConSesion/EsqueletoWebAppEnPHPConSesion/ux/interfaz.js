// ============================================================================
// Proyecto: Biometría y Medio Ambiente
// Asignatura: Proyecto Biometría (PBIO) - Sprint 0
// Archivo: interfaz.js
// Descripción: Controlador de la interfaz gráfica de usuario.
//              Conecta los elementos visuales (DOM) con la capa de abstracción logicaFake.
//
// ----------------------------------------------------------------------------
// () -> actualizarVista() -> Actualiza Tarjeta Principal, Tabla e Historial
// ----------------------------------------------------------------------------
// ============================================================================

// Mapeo amigable de identificadores de sensor a nombres y unidades de gas
var NOMBRES_SENSORES = {
    11: { nombre: 'Dióxido de Carbono (CO2)', unidad: 'ppm', limiteAlerta: 1000, limitePeligro: 1500 },
    12: { nombre: 'Temperatura Ambiental', unidad: '°C', limiteAlerta: 30, limitePeligro: 38 },
    13: { nombre: 'Nivel de Ruido Acústico', unidad: 'dB', limiteAlerta: 70, limitePeligro: 85 }
};

var idIntervaloAutoRefresco = null;

/**
 * Determina el nivel de calidad/alerta y la clase CSS según el sensor y valor medido.
 *
 * @param {number} sensor Código del sensor
 * @param {number} valor Magnitud leída
 * @returns {Object} Objeto con {texto, clase}
 */
function evaluarCalidad(sensor, valor) {
    var info = NOMBRES_SENSORES[sensor];
    if (!info) {
        return { texto: 'Medición Normal', clase: 'calidad-optima' };
    }

    if (valor >= info.limitePeligro) {
        return { texto: 'Alerta Crítica', clase: 'calidad-alerta' };
    } else if (valor >= info.limiteAlerta) {
        return { texto: 'Nivel Moderado', clase: 'calidad-moderada' };
    } else {
        return { texto: 'Óptimo / Seguro', clase: 'calidad-optima' };
    }
}

/**
 * Actualiza la tarjeta principal con los datos de una medición específica.
 *
 * @param {Object} m Objeto de medición {id, sensor, valor, contador, tiempo}
 */
function actualizarTarjetaPrincipal(m) {
    if (!m) return;

    var elValor = document.getElementById('valorPrincipal');
    var elUnidad = document.getElementById('unidadPrincipal');
    var elNombreGas = document.getElementById('nombreGasPrincipal');
    var elBadgeCalidad = document.getElementById('badgeCalidad');
    var elSensorId = document.getElementById('sensorIdPrincipal');
    var elContador = document.getElementById('contadorPrincipal');
    var elTiempo = document.getElementById('tiempoPrincipal');

    var infoSensor = NOMBRES_SENSORES[m.sensor] || {
        nombre: 'Sensor ' + m.sensor,
        unidad: 'uds.'
    };

    if (elValor) elValor.textContent = m.valor;
    if (elUnidad) elUnidad.textContent = infoSensor.unidad;
    if (elNombreGas) elNombreGas.textContent = infoSensor.nombre;
    if (elSensorId) elSensorId.textContent = m.sensor;
    if (elContador) elContador.textContent = '#' + m.contador;
    if (elTiempo) elTiempo.textContent = m.tiempo;

    if (elBadgeCalidad) {
        var estado = evaluarCalidad(m.sensor, m.valor);
        elBadgeCalidad.textContent = estado.texto;
        elBadgeCalidad.className = 'badge-calidad ' + estado.clase;
    }
}

/**
 * Actualiza la tabla del histórico con la lista de mediciones recibidas.
 *
 * @param {Array} lista Array de objetos de medición
 */
function actualizarTablaHistorico(lista) {
    var tbody = document.getElementById('cuerpoTablaHistorico');
    if (!tbody) return;

    tbody.innerHTML = '';

    if (!Array.isArray(lista) || lista.length === 0) {
        var filaVacia = document.createElement('tr');
        filaVacia.innerHTML = '<td colspan="6" style="text-align:center; padding: 2rem; color: #64748b;">No hay mediciones registradas todavía.</td>';
        tbody.appendChild(filaVacia);
        return;
    }

    lista.forEach(function(m) {
        var info = NOMBRES_SENSORES[m.sensor] || { nombre: 'Sensor ' + m.sensor, unidad: '' };
        var fila = document.createElement('tr');
        fila.innerHTML = '<td><strong>#' + m.id + '</strong></td>' +
                         '<td>' + m.sensor + '</td>' +
                         '<td>' + info.nombre + '</td>' +
                         '<td><strong>' + m.valor + ' ' + info.unidad + '</strong></td>' +
                         '<td>' + m.contador + '</td>' +
                         '<td>' + m.tiempo + '</td>';
        tbody.appendChild(fila);
    });
}

/**
 * Actualiza la barra de estado con la última consulta exitosa o mensaje de error.
 *
 * @param {boolean} exito
 * @param {string} [mensajeError]
 */
function actualizarBarraEstado(exito, mensajeError) {
    var elHora = document.getElementById('horaUltimaConsulta');
    var elMensaje = document.getElementById('mensajeEstado');

    if (exito) {
        var ahora = new Date();
        var horaFormateada = ahora.toLocaleTimeString();
        if (elHora) elHora.textContent = horaFormateada;
        if (elMensaje) elMensaje.textContent = 'Actualización completada';
    } else {
        if (elMensaje) elMensaje.textContent = mensajeError || 'Error de conexión';
    }
}

/**
 * Inicia el proceso de consulta a la capa de lógica (vía logicaFake) y refresca la interfaz.
 */
function actualizarVista() {
    var elMensaje = document.getElementById('mensajeEstado');
    if (elMensaje) elMensaje.textContent = 'Consultando servidor...';

    // 1. Obtener la última medición para la tarjeta destacada
    obtenerUltimaMedicion(function(errUltima, medicion) {
        if (!errUltima && medicion) {
            actualizarTarjetaPrincipal(medicion);
        }

        // 2. Obtener el listado de las últimas 10 mediciones para la tabla
        obtenerUltimasMediciones(10, function(errLista, lista) {
            if (!errLista && Array.isArray(lista)) {
                actualizarTablaHistorico(lista);
                actualizarBarraEstado(true);
            } else {
                console.error('Error al actualizar histórico:', errLista);
                actualizarBarraEstado(false, errLista);
            }
        });
    });
}

/**
 * Configura o detiene el auto-refresco periódico en función del checkbox.
 */
function toggleAutoRefresco() {
    var chk = document.getElementById('chkAutoRefresco');
    if (!chk) return;

    if (chk.checked) {
        if (idIntervaloAutoRefresco === null) {
            // Auto refresco cada 4 segundos
            idIntervaloAutoRefresco = setInterval(actualizarVista, 4000);
            console.log('Auto-refresco activado (cada 4s)');
        }
    } else {
        if (idIntervaloAutoRefresco !== null) {
            clearInterval(idIntervaloAutoRefresco);
            idIntervaloAutoRefresco = null;
            console.log('Auto-refresco desactivado');
        }
    }
}

// Inicialización de la página y eventos
document.addEventListener('DOMContentLoaded', function() {
    var btnActualizar = document.getElementById('btnActualizar');
    var chkAutoRefresco = document.getElementById('chkAutoRefresco');

    if (btnActualizar) {
        btnActualizar.addEventListener('click', function() {
            actualizarVista();
        });
    }

    if (chkAutoRefresco) {
        chkAutoRefresco.addEventListener('change', toggleAutoRefresco);
    }

    // Primera carga automática de datos
    actualizarVista();
});
