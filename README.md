# Proyecto Biometría y Medio Ambiente - Sprint 0

Este proyecto implementa la arquitectura completa de extremo a extremo para la captación, transmisión, almacenamiento y visualización de mediciones ambientales y de gases (CO2, Temperatura, Ruido):

```
+---------------+        BLE        +------------------+       REST        +------------------+
|  Arduino BLE  | --------------> |  Android (Proxy) | --------------> |   Servidor Web   |
| (iBeacon Em.) |                 |  (Servicio 2º P) |   HTTP POST/GET | (REST -> Lógica) |
+---------------+                 +------------------+                 +--------+---------+
                                                                                |
                                                                                v
                                                                       +------------------+
                                                                       |  Base de Datos   |
                                                                       | (MySQL / SQLite) |
                                                                       +------------------+
                                                                                ^
                                                                                |
+-------------------------------------------------------------------------------+
| Cliente Web Navegador: /ux/Aplicacion.html (HTML5, CSS Accesible, logicaFake)
+-------------------------------------------------------------------------------+
```

---

## 📁 Estructura del Proyecto

```
Sprint0_Imanol/
├── bbdd/                                  # 1. Capa de Base de Datos
│   ├── crear_bbdd.sql                     # Script DDL de creación de la BBDD y tabla 'mediciones'
│   ├── verificar_bbdd.sql                 # Script SQL de verificación (Test 1, Test 2, Test 3)
│   └── test_bbdd.php                      # Test de verificación automática de BBDD
│
├── logica/                                # 2. Capa de Lógica de Negocio (3 Capas)
│   ├── ConexionBD.php                     # Gestor de conexión PDO (MySQL + fallback SQLite transparente)
│   ├── guardarMedicion.php                # Función guardarMedicion(sensor, valor, contador, tiempo)
│   ├── obtenerUltimasMediciones.php       # Función obtenerUltimasMediciones(limite)
│   ├── obtenerUltimaMedicion.php          # Función obtenerUltimaMedicion(sensor)
│   └── test_logica.php                    # Batería de tests automáticos de la lógica
│
├── rest/                                  # 3. Capa de Servidor REST
│   ├── guardarMedicion.php                # Endpoint Receptor HTTP POST
│   ├── obtenerMediciones.php              # Endpoint Consultor HTTP GET
│   └── test_rest.php                      # Batería de tests automáticos para los endpoints REST
│
├── android/                               # 4. Capa Android (Fachada / Proxy de Red)
│   ├── PeticionarioREST.java              # Cliente asíncrono HTTP (AsyncTask)
│   ├── ProxyRedMediciones.java            # Fachada Proxy de red para desacoplar HTTP de la app
│   ├── ServicioEscucharYReenviar.java     # Servicio en segundo plano que une BLE y REST
│   └── TestProxyRed.java                  # Test de verificación del proxy y desempaquetado de tramas
│
├── ux/                                    # 5 y 6. Capa de Usuario (Interfaz Web y logicaFake)
│   ├── Aplicacion.html                    # Interfaz gráfica principal (HTML5 accesible, tarjeta, tabla)
│   ├── estilos.css                        # CSS moderno, responsivo y accesible (WCAG)
│   ├── interfaz.js                        # Controlador del DOM y gestión de auto-refresco periódico
│   ├── test_ux.html                       # Tests automáticos de la interfaz y del DOM
│   └── logicaFake/                        # Capa de Abstracción para el Navegador
│       ├── guardarMedicion.js             # Función fake para invocar POST ../rest/guardarMedicion.php
│       ├── obtenerMediciones.js           # Funciones fake para invocar GET ../rest/obtenerMediciones.php
│       ├── test_logicaFake.js             # Batería de pruebas de logicaFake
│       └── test_logicaFake.html           # Ejecutor visual de los tests de logicaFake
│
└── arduino/                               # 7. Firmware Arduino
    ├── Medidor.h                          # Sensor de gases (CO2, temperatura, etc.)
    ├── Publicador.h                       # Empaquetado y emisión en balizas iBeacon BLE
    └── EmisionGases.ino                   # Sketch con bucle de medición y anuncio BLE
```

---

## 🚀 Puesta en Marcha

### 1. Iniciar el Servidor Web PHP
Desde la raíz del proyecto o desde `EsqueletoWebAppEnPHPConSesion/EsqueletoWebAppEnPHPConSesion/`:

```bash
php -S localhost:8080 -t .
```

*(Si utilizas XAMPP en Windows: `C:\xampp\php\php.exe -S localhost:8080 -t .`)*

### 2. Acceder a la Aplicación Web
Abre en tu navegador web:
- Aplicación Principal: **`http://localhost:8080/ux/Aplicacion.html`**

---

## 🧪 Ejecución de Tests de Verificación Automáticos

### A. Tests de Base de Datos (PHP CLI)
```bash
php bbdd/test_bbdd.php
```
- **Test 1**: Inserta una medición con valores simulados.
- **Test 2**: Consulta y verifica los valores de la última medición insertada.
- **Test 3**: Test de Integridad (comprueba que los campos `NOT NULL` rechazan nulos).

### B. Tests de Lógica de Negocio (PHP CLI)
```bash
php logica/test_logica.php
```
- **Test 1**: Comprueba retorno booleano `true` al guardar medición válida.
- **Test 2**: Verifica que el valor del último objeto guardado coincide exactamente.
- **Test 3**: Verifica que la lista no está vacía y cumple la estructura `{id, sensor, valor, contador, tiempo}`.
- **Test 4**: Verifica que ante campos nulos o no válidos devuelve `false`.

### C. Tests de Servidor REST (PHP CLI)
```bash
php rest/test_rest.php
```
- **Test 1**: Petición POST con JSON válido devuelve HTTP 200 y `{ "resultado": true }`.
- **Test 2**: Petición POST con JSON vacío devuelve HTTP 400 y mensaje de error.
- **Test 3**: Validación de método incorrecto (GET a endpoint POST) devuelve HTTP 405.
- **Test 4**: Petición GET a `obtenerMediciones.php` devuelve array JSON válido con el límite especificado.

### D. Tests de Abstracción en Cliente (`logicaFake`)
Abre en el navegador:
- **`http://localhost:8080/ux/logicaFake/test_logicaFake.html`**

### E. Tests de la Interfaz de Usuario (`ux`)
Abre en el navegador:
- **`http://localhost:8080/ux/test_ux.html`**

---

## 📋 Notación de Asignatura Utilizada

Todas las funciones siguen estrictamente la signatura formal:

```
(sensor:Z, valor:Z, contador:N, tiempo:Texto) -> guardarMedicion() -> Booleano | error:Texto
limite:N -> obtenerUltimasMediciones() -> Lista<Medicion> | error:Texto
sensor:Z -> obtenerUltimaMedicion() -> Medicion | null
```

