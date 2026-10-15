package org.jordi.biometria;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

// ============================================================================
// Proyecto: Biometría y Medio Ambiente
// Asignatura: Proyecto Biometría (PBIO) - Sprint 0
// Archivo: ServicioEscucharYReenviar.java
// Descripción: Servicio Android en segundo plano que escucha balizas BLE de Arduino
//              y reenvía automáticamente las lecturas al servidor mediante ProxyRedMediciones.
// ============================================================================

public class ServicioEscucharYReenviar extends Service {

    private static final String ETIQUETA_LOG = "ServicioReenvio";
    private ProxyRedMediciones proxyRed;
    private boolean activo = false;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(ETIQUETA_LOG, "ServicioEscucharYReenviar creado");
        this.proxyRed = new ProxyRedMediciones();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(ETIQUETA_LOG, "Servicio iniciado en segundo plano");
        this.activo = true;
        return START_STICKY;
    }

    /**
     * Invocado cuando el receptor BLE detecta una trama iBeacon emitida por Arduino.
     * Extrae sensor, contador y valor, y los reenvía por REST usando la fachada ProxyRedMediciones.
     *
     * @param major Campo major del iBeacon (Sensor << 8 + contador)
     * @param minor Campo minor del iBeacon (Valor medido del gas)
     */
    public void procesarTramaBluetooth(int major, int minor) {
        if (!activo) return;

        // Desempaquetado según especificación de Publicador.h:
        // uint16_t major = (MedicionesID::CO2 << 8) + contador;
        int sensor = (major >> 8) & 0xFF;
        int contador = major & 0xFF;
        int valor = minor;

        Log.d(ETIQUETA_LOG, "Trama decodificada -> Sensor: " + sensor + ", Contador: " + contador + ", Valor: " + valor);

        // Reenvío inmediato a través de la fachada de red REST
        this.proxyRed.guardarMedicion(sensor, valor, contador, new ProxyRedMediciones.CallbackGuardarMedicion() {
            @Override
            public void alCompletar(boolean exito, String mensaje) {
                if (exito) {
                    Log.d(ETIQUETA_LOG, "Medición reenviada con éxito al servidor REST");
                } else {
                    Log.e(ETIQUETA_LOG, "Error reenviando medición al servidor: " + mensaje);
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.activo = false;
        Log.d(ETIQUETA_LOG, "Servicio destruido");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

