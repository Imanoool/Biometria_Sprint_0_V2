package es.biometria.gateway;

/**
 * ----------------------------------------------------
 * Archivo: GatewayCodec.java
 * Autor: Imanol Figuero Parras
 * Fecha: 2026
 * Proyecto: Biometría y Monitorización - Sprint 0
 * Descripción: Fachada Java para src/android/.
 * ----------------------------------------------------
 */
public class GatewayCodec {
    /**
     * ----------------------------------------------------
     * Analiza bytes de trama iBeacon.
     * @param bytes [Z]
     * @return TramaIBeacon
     * ----------------------------------------------------
     */
    public static TramaIBeacon analizar(byte[] bytes) {
        return new TramaIBeacon(bytes);
    }
}
