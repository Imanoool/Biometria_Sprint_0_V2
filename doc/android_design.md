<!-- Nombre del archivo: android_design.md
Autor: Imanol Figuero Parras
Fecha: 2026
Proyecto: Biometría y Monitorización - Sprint 0
Descripción: Gateway Java. Copyright: 2026 Imanol Figuero Parras.
Aportación: validación binaria y transporte asíncrono. -->
# Component Design
```text
                         -------- TramaIBeacon --------------------
                         | bytes: [Z]
                         | valida: B
bytes: [Z]           --> TramaIBeacon() -->
                         |
                         |
valida: B            <-- esValida() <--
                         |
                         |
major: N             <-- getMajorInt() <--
                         |
                         |
minor: N             <-- getMinorInt() <--
                         |
                         |
tipo: N              <-- getTipoMedicion() <--
                         |
                         |
valor: Z             <-- getValor() <--
                         |
                         |
contador: N          <-- getContador() <--
                         |
                         -----------------------------------------
bytes: [Z] --> bytesToIntOK() --x --> valor: Z
bytes: [Z] --> bytesToUnsignedInt() --x --> valor: N
bytes: [Z] --> bytesToHexString() --x --> texto: Text
texto: Text --> stringToBytes() --x --> bytes: [Z]
bytes: [Z] --> bytesToString() --x --> texto: Text
metodo: Text, url: Text, cuerpo: Text --> hacerPeticionREST() --> respuesta: RespuestaREST
close() -->
tipo: N, valor: Z --> simularRecepcionBeacon() --x --> bytes: [Z]_30
```
# Design Clarifications
- `ClienteREST` tiene dos implementaciones intercambiables: real y fake.
- Respuesta asíncrona, tiempo máximo y cola acotada; UTF-8 explícito y cierre incondicional.
- MainActivity coordina permisos, inicio/parada en primer plano, lectura de tramas, envío y estado UI.
- Para API 23..30 solicita ubicación; desde 31 SCAN/CONNECT. El escaneo se para al salir.
- Filtra UUID del proyecto, descarta repeticiones durante 1 segundo; no promete exactly-once.
- Fuente: `android/app/src/main/java/es/biometria/`; fachada `src/android/GatewayCodec.java`.
# General Rules
- Programming Language: Java 11, Android SDK 35, JDK 17 para Gradle.
- Function/Method Headers: firma v3 delimitada con `--------------------`.
- Code Readability: sin AsyncTask, dependencias Android fuera del codec.
- Automated Testing: JUnit 4 sobre conversiones, tramas y fake; dispositivo físico para permisos/radio.
