// ---------------------------------------------------
//
// versión fake de una función de la lógica
//
// (sensor:Z, valor:Z, contador:N, tiempo:Texto) -> guardarMedicion() -> (resultado:Booleano) | error:Texto
//
// (resultado:Booleano) | error:Texto : devuelto via callback( err, res )
//
// ---------------------------------------------------

/**
 * Versión fake de la función de la lógica guardarMedicion.
 * Realiza una petición POST al endpoint REST abstraer la red para la UI.
 *
 * @param {Object|number} arg1 Objeto con {sensor, valor, contador, tiempo} o el sensor (Z)
 * @param {number|Function} arg2 Valor (Z) o función callback si arg1 es un objeto
 * @param {number} [arg3] Contador (N)
 * @param {string} [arg4] Tiempo (Texto)
 * @param {Function} [arg5] Callback(err, res)
 */
function guardarMedicion(arg1, arg2, arg3, arg4, arg5) {
    var payload;
    var cb;

    // Soporte tanto para paso de objeto {sensor, valor, ...} como parámetros sueltos
    if (typeof arg1 === 'object' && arg1 !== null) {
        payload = {
            sensor: arg1.sensor,
            valor: arg1.valor,
            contador: arg1.contador,
            tiempo: arg1.tiempo || new Date().toISOString().replace('T', ' ').substring(0, 19)
        };
        cb = arg2;
    } else {
        payload = {
            sensor: arg1,
            valor: arg2,
            contador: arg3,
            tiempo: arg4 || new Date().toISOString().replace('T', ' ').substring(0, 19)
        };
        cb = arg5;
    }

    if (typeof cb !== 'function') {
        cb = function() {};
    }

    // Usar Fetch API moderna con compatibilidad hacia XMLHttpRequest
    if (typeof window !== 'undefined' && window.fetch) {
        fetch('../rest/guardarMedicion.php', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json; charset=utf-8',
                'Accept': 'application/json'
            },
            body: JSON.stringify(payload)
        })
        .then(function(response) {
            return response.json().then(function(data) {
                if (!response.ok) {
                    throw new Error(data.error || ('Error HTTP ' + response.status));
                }
                return data;
            });
        })
        .then(function(data) {
            cb(null, data);
        })
        .catch(function(err) {
            console.error('Error en guardarMedicion fake:', err.message);
            cb(err.message, null);
        });
    } else {
        // Fallback robusto XMLHttpRequest
        var xmlhttp = new XMLHttpRequest();
        xmlhttp.onreadystatechange = function() {
            if (this.readyState === 4) {
                if (this.status === 200 || this.status === 201) {
                    try {
                        var res = JSON.parse(this.responseText);
                        cb(null, res);
                    } catch (e) {
                        cb('Error parseando JSON: ' + e.message, null);
                    }
                } else {
                    var msg = 'Error HTTP ' + this.status;
                    try {
                        var errObj = JSON.parse(this.responseText);
                        if (errObj.error) msg = errObj.error;
                    } catch (e) {}
                    cb(msg, null);
                }
            }
        };
        xmlhttp.open('POST', '../rest/guardarMedicion.php', true);
        xmlhttp.setRequestHeader('Content-Type', 'application/json; charset=utf-8');
        xmlhttp.send(JSON.stringify(payload));
    }
}

