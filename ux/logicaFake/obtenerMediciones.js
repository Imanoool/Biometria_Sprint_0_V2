// ---------------------------------------------------
//
// versión fake de una función de la lógica
//
// limite:N -> obtenerUltimasMediciones() -> Lista<Medicion> | error:Texto
// () -> obtenerUltimaMedicion() -> Medicion | error:Texto
//
// devueltos via callback( err, res )
//
// ---------------------------------------------------

/**
 * Versión fake de la función obtenerUltimasMediciones.
 * Realiza una petición GET al endpoint REST con el límite especificado.
 *
 * @param {number} limite Cantidad máxima de mediciones a recuperar (N)
 * @param {Function} cb Callback con firma cb(err, res) donde res es un array de mediciones
 */
function obtenerUltimasMediciones(limite, cb) {
    if (typeof cb !== 'function') {
        cb = function() {};
    }

    var url = '../rest/obtenerMediciones.php';
    if (typeof limite === 'number' && limite > 0) {
        url += '?limite=' + encodeURIComponent(limite);
    }

    // Usar Fetch API con fallback compatible a XMLHttpRequest
    if (typeof window !== 'undefined' && window.fetch) {
        fetch(url, {
            method: 'GET',
            headers: {
                'Accept': 'application/json'
            }
        })
        .then(function(response) {
            if (!response.ok) {
                throw new Error('Fallo al consultar mediciones: HTTP ' + response.status);
            }
            return response.json();
        })
        .then(function(datos) {
            cb(null, datos);
        })
        .catch(function(err) {
            console.error('Error en obtenerUltimasMediciones:', err.message);
            cb('Error de conexión con el servidor: ' + err.message, null);
        });
    } else {
        var xmlhttp = new XMLHttpRequest();
        xmlhttp.onreadystatechange = function() {
            if (this.readyState === 4) {
                if (this.status === 200) {
                    try {
                        var datos = JSON.parse(this.responseText);
                        cb(null, datos);
                    } catch (e) {
                        cb('Error al decodificar respuesta JSON: ' + e.message, null);
                    }
                } else {
                    cb('Error en el servidor: HTTP ' + this.status, null);
                }
            }
        };
        xmlhttp.open('GET', url, true);
        xmlhttp.send();
    }
}

/**
 * Versión fake de la función obtenerUltimaMedicion.
 * Realiza una llamada HTTP GET sin límite para obtener únicamente la medición más reciente.
 *
 * @param {Function} cb Callback con firma cb(err, res) donde res es el objeto de la última medición
 */
function obtenerUltimaMedicion(cb) {
    if (typeof cb !== 'function') {
        cb = function() {};
    }

    // Petición al endpoint REST con parámetro ultima=true (o sin límite)
    var url = '../rest/obtenerMediciones.php?ultima=true';

    if (typeof window !== 'undefined' && window.fetch) {
        fetch(url, {
            method: 'GET',
            headers: {
                'Accept': 'application/json'
            }
        })
        .then(function(response) {
            if (!response.ok) {
                throw new Error('Fallo de conexión al servidor: HTTP ' + response.status);
            }
            return response.json();
        })
        .then(function(dato) {
            // Si la respuesta es un array, tomamos el primer elemento (el más reciente)
            var medicion = Array.isArray(dato) ? (dato.length > 0 ? dato[0] : null) : dato;
            cb(null, medicion);
        })
        .catch(function(err) {
            console.error('Error en obtenerUltimaMedicion:', err.message);
            cb('Fallo al obtener la última medición: ' + err.message, null);
        });
    } else {
        var xmlhttp = new XMLHttpRequest();
        xmlhttp.onreadystatechange = function() {
            if (this.readyState === 4) {
                if (this.status === 200) {
                    try {
                        var dato = JSON.parse(this.responseText);
                        var medicion = Array.isArray(dato) ? (dato.length > 0 ? dato[0] : null) : dato;
                        cb(null, medicion);
                    } catch (e) {
                        cb('Error al recibir JSON de la última medición: ' + e.message, null);
                    }
                } else {
                    cb('Error en el servidor: HTTP ' + this.status, null);
                }
            }
        };
        xmlhttp.open('GET', url, true);
        xmlhttp.send();
    }
}

