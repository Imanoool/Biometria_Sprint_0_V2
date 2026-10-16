<!-- Nombre del archivo: firmware_design.md
Autor: Imanol Figuero Parras
Fecha: 2026
Proyecto: Biometría y Monitorización - Sprint 0
Descripción: Emisión BLE. Copyright: 2026 Imanol Figuero Parras.
Aportación: encapsulación y bytes deterministas. -->
# Component Design
```text
                          -------- EmisoraBLE --------------------
                          | inicializada: B
                          |
                      --> encenderEmisora() -->
                          |
                          |
tipo: N, contador: N, valor: Z --> emitirAnuncioIBeacon() -->
exito: B              <--
                          |
                          |
                          detenerAnuncio() -->
                          |
                          ---------------------------------------
                          -------- Publicador --------------------
                          | emisora: EmisoraBLE
                          | contador: N
                          |
                          encenderEmisora() -->
                          |
                          |
valor: Z              --> publicarCO2() -->
exito: B              <--
                          |
                          |
valor: Z              --> publicarTemperatura() -->
exito: B              <--
                          |
                          ---------------------------------------
setup() -->
loop() -->
tipo: N, contador: N, valor: Z --> construirTrama() --x --> trama: [N]_30
```
# Design Clarifications
- Backend radio: Adafruit Bluefruit nRF52. Esquema de bytes según especificaciones.
- Publicar detiene anuncio anterior; nunca añade nombre ni scan response al frame.
- Sin espera USB bloqueante; alterna CO2=650 y temperatura=23 cada dos segundos.
- Fuente `firmware/`; `src/firmware/Publicador.h` incluye la cabecera canónica.
# General Rules
- Programming Language: C++11 Arduino.
- Function/Method Headers: firma v3 delimitada con `--------------------`.
- Code Readability: búfer inicializado; propiedades encapsuladas.
- Automated Testing: vector compartido y compilación con core Adafruit; radio exige placa física.
