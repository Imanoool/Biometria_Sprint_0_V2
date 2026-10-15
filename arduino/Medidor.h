// -*- mode: c++ -*-
// ============================================================================
// Proyecto: Biometría y Medio Ambiente
// Asignatura: Proyecto Biometría (PBIO) - Sprint 0
// Archivo: Medidor.h
// Descripción: Clase encargada de la lectura de los sensores de gases (CO2, etc.)
// ============================================================================

#ifndef MEDIDOR_H_INCLUIDO
#define MEDIDOR_H_INCLUIDO

#include <Arduino.h>

class Medidor {
private:
    int pinSensorCO2;

public:
    Medidor(int pin = A0) : pinSensorCO2(pin) {}

    void iniciarMedidor() {
        // Inicialización de hardware o sensores si fuese necesario
    }

    /**
     * Mide la concentración de CO2 en partes por millón (ppm).
     * En simulación devuelve un valor representativo realista (ej. 400 - 800 ppm).
     */
    int medirCO2() {
        // Simulación de lectura analógica o de sensor específico
        // Para pruebas reales se leería el pin analógico o bus I2C/UART
        return 420 + (rand() % 60);
    }

    /**
     * Mide la temperatura ambiente en grados Celsius.
     */
    int medirTemperatura() {
        return 22 + (rand() % 4);
    }

    /**
     * Mide el nivel de ruido acústico en decibelios (dB).
     */
    int medirRuido() {
        return 45 + (rand() % 25);
    }
};

#endif

