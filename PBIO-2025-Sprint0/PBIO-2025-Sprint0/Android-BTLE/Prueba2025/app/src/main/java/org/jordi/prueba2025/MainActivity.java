package org.jordi.prueba2025;

import android.bluetooth.le.ScanSettings;
import android.support.v7.app.AppCompatActivity;

import android.support.v4.content.ContextCompat;
import android.support.v4.app.ActivityCompat;

import android.os.Bundle;
import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {

    // --------------------------------------------------------------
    // URL del servidor REST:
    //   - Emulador Android Studio: 10.0.2.2 (apunta al localhost del PC)
    //   - Móvil físico: poner la IP local del PC (ej: 192.168.1.47)
    // --------------------------------------------------------------
    private static final String URL_SERVIDOR = "http://192.168.1.47:8080";
    // private static final String URL_SERVIDOR = "http://192.168.1.47:8080"; // Descomentar para móvil físico

    // UUID del beacon que emite nuestra placa Arduino (definido en Publicador.h)
    private static final String BEACON_UUID_ESPERADO = "EPSG-GTI-PROY-3A";

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    private static final String ETIQUETA_LOG = ">>>>";

    // En Android Support Library, el requestCode DEBE ser menor de 16 bits (máximo 65535)
    private static final int CODIGO_PETICION_PERMISOS = 1001;

    // Proxy de red para comunicar con el servidor REST
    private ProxyRedMediciones proxyRed;

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    private BluetoothLeScanner elEscanner;

    private ScanCallback callbackDelEscaneo = null;

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    // --------------------------------------------------------------
    // Comprobación y solicitud de permisos para Android 12+ (API 31/32)
    // --------------------------------------------------------------
    private boolean comprobarOPedirPermisos() {
        List<String> faltan = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            faltan.add(Manifest.permission.BLUETOOTH_SCAN);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            faltan.add(Manifest.permission.BLUETOOTH_CONNECT);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            faltan.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            faltan.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }

        if (!faltan.isEmpty()) {
            Log.w(ETIQUETA_LOG, "comprobarOPedirPermisos(): Faltan permisos: " + faltan + " -> Solicitando ahora...");
            requestPermissions(faltan.toArray(new String[0]), CODIGO_PETICION_PERMISOS);
            return false;
        }

        Log.d(ETIQUETA_LOG, "comprobarOPedirPermisos(): Todos los permisos necesarios están concedidos.");
        return true;
    }

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    private void buscarTodosLosDispositivosBTLE() {
        Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTLE(): empieza ");

        detenerBusquedaDispositivosBTLE();

        if (!comprobarOPedirPermisos()) {
            Log.w(ETIQUETA_LOG, " buscarTodosLosDispositivosBTLE(): Permisos pendientes, esperando autorización...");
            return;
        }

        if (this.elEscanner == null) {
            BluetoothAdapter bta = BluetoothAdapter.getDefaultAdapter();
            if (bta != null) this.elEscanner = bta.getBluetoothLeScanner();
        }

        if (this.elEscanner == null) {
            Log.e(ETIQUETA_LOG, " buscarTodosLosDispositivosBTLE(): No se pudo obtener el escáner Bluetooth LE.");
            return;
        }

        this.callbackDelEscaneo = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult resultado) {
                super.onScanResult(callbackType, resultado);
                if (resultado != null && resultado.getDevice() != null) {
                    Log.d(ETIQUETA_LOG, ">>> BLE Detectado: " + resultado.getDevice().getAddress() + " (rssi=" + resultado.getRssi() + "dBm)");
                }
                mostrarInformacionDispositivoBTLE(resultado);
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                super.onBatchScanResults(results);
                for (ScanResult r : results) {
                    mostrarInformacionDispositivoBTLE(r);
                }
            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
                Log.e(ETIQUETA_LOG, " buscarTodosLosDispositivosBTLE(): onScanFailed() código de error = " + errorCode);
            }
        };

        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();

        Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTLE(): startScan activo en modo LOW_LATENCY...");
        try {
            this.elEscanner.startScan(null, settings, this.callbackDelEscaneo);
            Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTLE(): ¡Escáner iniciado con éxito!");
        } catch (SecurityException se) {
            Log.e(ETIQUETA_LOG, " SecurityException al iniciar escaneo: " + se.getMessage());
        }
    } // ()

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    private void mostrarInformacionDispositivoBTLE(ScanResult resultado) {
        if (resultado == null || resultado.getScanRecord() == null) {
            return;
        }

        BluetoothDevice bluetoothDevice = resultado.getDevice();
        byte[] bytes = resultado.getScanRecord().getBytes();
        int rssi = resultado.getRssi();

        if (bytes == null || bytes.length < 20) {
            return;
        }

        String nombre = "Desconocido";
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
            try {
                if (bluetoothDevice.getName() != null) {
                    nombre = bluetoothDevice.getName();
                }
            } catch (Exception ignored) {}
        }

        // Si el dispositivo está en rango útil (> -80dBm), registramos HEX y ASCII para diagnóstico
        if (rssi >= -80) {
            String asciiLimpio = new String(bytes).replaceAll("[^\\x20-\\x7E]", ".");
            Log.d(ETIQUETA_LOG, ">> [DISP " + bluetoothDevice.getAddress() + "] rssi=" + rssi + "dBm | ASCII: " + asciiLimpio);
        }

        int sensor = 0;
        int contador = 0;
        int valor = 0;
        boolean esNuestroBeacon = false;

        // Estrategia 1: TramaIBeacon estándar con offsets fijos
        try {
            if (bytes.length >= 30) {
                TramaIBeacon tib = new TramaIBeacon(bytes);
                String uuidTexto = Utilidades.bytesToString(tib.getUUID());
                String uuidInverso = new StringBuilder(uuidTexto).reverse().toString();

                if ((uuidTexto != null && (uuidTexto.contains("EPSG") || uuidTexto.contains("GTI") || uuidTexto.contains(BEACON_UUID_ESPERADO))) ||
                    (uuidInverso != null && uuidInverso.contains("EPSG"))) {
                    int major = Utilidades.bytesToInt(tib.getMajor());
                    int minor = Utilidades.bytesToInt(tib.getMinor());
                    sensor = (major >> 8) & 0xFF;
                    contador = major & 0xFF;
                    valor = (short) minor;
                    esNuestroBeacon = true;
                    Log.d(ETIQUETA_LOG, ">> [MATCH ESTRATEGIA 1 - TramaIBeacon] UUID=" + uuidTexto);
                }
            }
        } catch (Exception e) {
            Log.d(ETIQUETA_LOG, ">> Error Estrategia 1: " + e.getMessage());
        }

        // Estrategia 2: Búsqueda flexible de la subcadena en cualquier offset
        if (!esNuestroBeacon) {
            String raw = new String(bytes);
            String asAscii = Utilidades.bytesToString(bytes);
            String[] patrones = new String[]{BEACON_UUID_ESPERADO, "EPSG-GTI", "GTI-PROY", "GTI-3A"};

            for (String patron : patrones) {
                int idx = raw.indexOf(patron);
                if (idx == -1) {
                    idx = asAscii.indexOf(patron);
                }
                if (idx != -1 && idx + 20 <= bytes.length) {
                    int major = ((bytes[idx + 16] & 0xFF) << 8) | (bytes[idx + 17] & 0xFF);
                    int minor = ((bytes[idx + 18] & 0xFF) << 8) | (bytes[idx + 19] & 0xFF);
                    sensor = (major >> 8) & 0xFF;
                    contador = major & 0xFF;
                    valor = (short) minor;
                    esNuestroBeacon = true;
                    Log.d(ETIQUETA_LOG, ">> [MATCH ESTRATEGIA 2 - Offset " + idx + "] Patron=" + patron);
                    break;
                }
            }
        }

        // Estrategia 3: Coincidencia por nombre de dispositivo (GTI-3A o GTI3A)
        if (!esNuestroBeacon && (nombre.contains("GTI") || nombre.contains("3A"))) {
            try {
                if (bytes.length >= 30) {
                    TramaIBeacon tib = new TramaIBeacon(bytes);
                    int major = Utilidades.bytesToInt(tib.getMajor());
                    int minor = Utilidades.bytesToInt(tib.getMinor());
                    sensor = (major >> 8) & 0xFF;
                    contador = major & 0xFF;
                    valor = (short) minor;
                    esNuestroBeacon = true;
                    Log.d(ETIQUETA_LOG, ">> [MATCH ESTRATEGIA 3 - Nombre " + nombre + "]");
                }
            } catch (Exception ignored) {}
        }

        // ----------------------------------------------------------
        // ¡NUESTRO BEACON FUE ENCONTRADO!
        // ----------------------------------------------------------
        if (esNuestroBeacon) {
            final int fSensor = sensor;
            final int fContador = contador;
            final int fValor = valor;

            Log.i(ETIQUETA_LOG, " ");
            Log.i(ETIQUETA_LOG, "*****************************************************");
            Log.i(ETIQUETA_LOG, "**** ¡¡¡NUESTRO BEACON DETECTADO DEL ARDUINO!!! ****");
            Log.i(ETIQUETA_LOG, "  Dispositivo: " + nombre + " (" + bluetoothDevice.getAddress() + ")");
            Log.i(ETIQUETA_LOG, "  SENSOR = " + fSensor + (fSensor == 11 ? " (CO2)" : (fSensor == 12 ? " (Temperatura)" : "")));
            Log.i(ETIQUETA_LOG, "  VALOR = " + fValor);
            Log.i(ETIQUETA_LOG, "  CONTADOR = " + fContador);
            Log.i(ETIQUETA_LOG, "  RSSI = " + rssi + " dBm");
            Log.i(ETIQUETA_LOG, "*****************************************************");

            // Enviar inmediatamente al servidor REST en segundo plano
            proxyRed.guardarMedicion(fSensor, fValor, fContador, new ProxyRedMediciones.CallbackGuardarMedicion() {
                @Override
                public void alCompletar(boolean exito, String mensaje) {
                    if (exito) {
                        Log.i(ETIQUETA_LOG, ">>> [REST OK] ¡MEDICIÓN GUARDADA EN BBDD CON ÉXITO! sensor=" + fSensor + ", valor=" + fValor);
                    } else {
                        Log.e(ETIQUETA_LOG, ">>> [REST ERROR] Fallo al enviar al servidor: " + mensaje);
                    }
                }
            });
        }
    } // ()

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    private void buscarEsteDispositivoBTLE(final String dispositivoBuscado) {
        Log.d(ETIQUETA_LOG, " buscarEsteDispositivoBTLE(): buscando dispositivo nuestro -> " + dispositivoBuscado);
        // Iniciamos el escaneo general; la función mostrarInformacionDispositivoBTLE
        // filtrará y capturará exclusivamente nuestro beacon por UUID ("EPSG-GTI-PROY-3A")
        this.buscarTodosLosDispositivosBTLE();
    } // ()

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    private void detenerBusquedaDispositivosBTLE() {
        Log.d(ETIQUETA_LOG, " detenerBusquedaDispositivosBTLE(): empieza");
        if (this.callbackDelEscaneo == null) {
            Log.d(ETIQUETA_LOG, " detenerBusquedaDispositivosBTLE(): no había escaneo activo");
            return;
        }

        try {
            if (this.elEscanner != null &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
                this.elEscanner.stopScan(this.callbackDelEscaneo);
                Log.d(ETIQUETA_LOG, " detenerBusquedaDispositivosBTLE(): escaneo detenido correctamente");
            }
        } catch (Exception e) {
            Log.e(ETIQUETA_LOG, " Error al detener escaneo: " + e.getMessage());
        }

        this.callbackDelEscaneo = null;
    } // ()

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    public void botonBuscarDispositivosBTLEPulsado(View v) {
        Log.d(ETIQUETA_LOG, "boton buscar dispositivos BTLE Pulsado");
        this.buscarTodosLosDispositivosBTLE();
    } // ()

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    public void botonBuscarNuestroDispositivoBTLEPulsado(View v) {
        Log.d(ETIQUETA_LOG, "boton nuestro dispositivo BTLE Pulsado");
        this.buscarEsteDispositivoBTLE("GTI-3A");
    } // ()

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    public void botonDetenerBusquedaDispositivosBTLEPulsado(View v) {
        Log.d(ETIQUETA_LOG, "boton detener busqueda dispositivos BTLE Pulsado");
        this.detenerBusquedaDispositivosBTLE();
    } // ()

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    private void inicializarBlueTooth() {
        Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): empieza ");

        BluetoothAdapter bta = BluetoothAdapter.getDefaultAdapter();
        if (bta == null) {
            Log.e(ETIQUETA_LOG, " inicializarBlueTooth(): Este dispositivo no soporta Bluetooth.");
            return;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
            if (!bta.isEnabled()) {
                bta.enable();
            }
        }

        this.elEscanner = bta.getBluetoothLeScanner();
        Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): escáner obtenido = " + (this.elEscanner != null));

        // Comprobamos y solicitamos permisos al iniciar la actividad
        comprobarOPedirPermisos();

        Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): TERMINA ");
    } // ()

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(ETIQUETA_LOG, " onCreate(): empieza ");

        // Inicializar el proxy de red con la URL del servidor REST
        this.proxyRed = new ProxyRedMediciones(URL_SERVIDOR);
        Log.d(ETIQUETA_LOG, " onCreate(): ProxyRedMediciones inicializado con URL=" + URL_SERVIDOR);

        inicializarBlueTooth();

        Log.d(ETIQUETA_LOG, " onCreate(): termina ");
    } // onCreate()

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Log.d(ETIQUETA_LOG, " onRequestPermissionResult(): código=" + requestCode);

        if (requestCode == CODIGO_PETICION_PERMISOS) {
            boolean todosConcedidos = true;
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    todosConcedidos = false;
                    Log.w(ETIQUETA_LOG, " Permiso denegado: " + permissions[i]);
                } else {
                    Log.d(ETIQUETA_LOG, " Permiso concedido: " + permissions[i]);
                }
            }

            if (todosConcedidos) {
                Log.d(ETIQUETA_LOG, " >>> TODOS LOS PERMISOS CONCEDIDOS CORRECTAMENTE <<< ");
                // Reinicializamos el escáner por si antes era null
                BluetoothAdapter bta = BluetoothAdapter.getDefaultAdapter();
                if (bta != null) {
                    this.elEscanner = bta.getBluetoothLeScanner();
                }
            } else {
                Log.e(ETIQUETA_LOG, " >>> ATENCIÓN: Algunos permisos no fueron concedidos. BLE puede fallar. <<< ");
            }
        }
    } // ()

} // class