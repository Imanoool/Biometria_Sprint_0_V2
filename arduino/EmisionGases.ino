// -*- mode: c++ -*-
// ============================================================================
// Proyecto: Biometría y Medio Ambiente
// Asignatura: Proyecto Biometría (PBIO) - Sprint 0
// Archivo: EmisionGases.ino
// Descripción: Programa principal Arduino para medir y emitir balizas periódicas
//              con lecturas de gases vía Bluetooth Low Energy (BLE) hacia Android.
// ============================================================================

#include <Arduino.h>
#include <bluefruit.h>
#include "Medidor.h"
#include "Publicador.h"

// Instancias globales
Medidor elMedidor;
Publicador elPublicador;
uint8_t contadorSecuencia = 0;

void setup() {
    Serial.begin(115200);
    // Esperar puerto serie opcional para depuración
    delay(1000);
    Serial.println("==================================================");
    Serial.println("  Arduino Emisor de Gases BLE (Sprint 0 - PBIO)   ");
    Serial.println("==================================================");

    // Inicializar sensor y emisora BLE
    elMedidor.iniciarMedidor();
    elPublicador.encenderEmisora();

    Serial.println("Sistema iniciado. Comenzando ciclo de medición y emisión...");
}

void loop() {
    contadorSecuencia++;

    Serial.print("\n[Muestreo #");
    Serial.print(contadorSecuencia);
    Serial.println("] Leyendo sensores...");

    // 1. Medir concentración de gas CO2
    int ppmCO2 = elMedidor.medirCO2();
    Serial.print(" -> CO2 medido: ");
    Serial.print(ppmCO2);
    Serial.println(" ppm");

    // 2. Publicar por Bluetooth Low Energy usando formato iBeacon
    Serial.println(" -> Emitiendo baliza iBeacon BLE...");
    elPublicador.publicarCO2(ppmCO2, contadorSecuencia, 1500); // 1.5s de emisión

    // 3. Medir y emitir temperatura opcional
    int temp = elMedidor.medirTemperatura();
    Serial.print(" -> Temperatura: ");
    Serial.print(temp);
    Serial.println(" °C");
    elPublicador.publicarTemperatura(temp, contadorSecuencia, 1000);

    Serial.println(" -> Fin de ciclo. Esperando siguiente intervalo...");
    delay(2500); // Espera entre ráfagas
}

