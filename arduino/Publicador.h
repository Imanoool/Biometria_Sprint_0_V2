// -*- mode: c++ -*-
// ============================================================================
// Proyecto: Biometría y Medio Ambiente
// Asignatura: Proyecto Biometría (PBIO) - Sprint 0
// Archivo: Publicador.h
// Descripción: Clase encargada de empaquetar y emitir las lecturas de los sensores
//              en formato de balizas iBeacon BLE compatibles con el receptor Android.
//
// Formato iBeacon empleado:
// - UUID: 'EPSG-GTI-PROY-3A' (16 bytes)
// - Major (2 bytes): (ID_Sensor << 8) + Contador_Secuencia
// - Minor (2 bytes): Valor_Medido
// - RSSI (1 byte): -53 dBm
// ============================================================================

#ifndef PUBLICADOR_H_INCLUIDO
#define PUBLICADOR_H_INCLUIDO

#include <bluefruit.h>

class Publicador {
private:
    uint8_t beaconUUID[16] = { 
        'E', 'P', 'S', 'G', '-', 'G', 'T', 'I', 
        '-', 'P', 'R', 'O', 'Y', '-', '3', 'A'
    };

public:
    const int RSSI = -53;

    // Identificadores de sensores de gas y biometría acordados
    enum MedicionesID {
        CO2 = 11,
        TEMPERATURA = 12,
        RUIDO = 13
    };

    Publicador() {}

    void encenderEmisora() {
        Bluefruit.begin();
        Bluefruit.setTxPower(4); // +4 dBm
        Bluefruit.setName("GTI3A-2025");
    }

    /**
     * Publica una medición de CO2 empaquetándola en la baliza iBeacon.
     *
     * @param valorCO2 Magnitud leída en ppm (minor)
     * @param contador Secuencia de Arduino (byte bajo del major)
     * @param tiempoEspera Milisegundos de emisión
     */
    void publicarCO2(int16_t valorCO2, uint8_t contador, long tiempoEspera) {
        uint16_t major = (MedicionesID::CO2 << 8) + contador;
        emitirIBeacon(major, valorCO2, tiempoEspera);
    }

    /**
     * Publica una medición de Temperatura.
     */
    void publicarTemperatura(int16_t valorTemperatura, uint8_t contador, long tiempoEspera) {
        uint16_t major = (MedicionesID::TEMPERATURA << 8) + contador;
        emitirIBeacon(major, valorTemperatura, tiempoEspera);
    }

private:
    void emitirIBeacon(uint16_t major, uint16_t minor, long tiempoEspera) {
        // Configuración de la baliza iBeacon
        BLEBeacon beacon(this->beaconUUID, major, minor, this->RSSI);
        beacon.setManufacturer(0x004c); // Apple iBeacon ID

        Bluefruit.Advertising.setBeacon(beacon);
        Bluefruit.Advertising.start();

        delay(tiempoEspera);

        Bluefruit.Advertising.stop();
    }
};

#endif

