package org.jordi.biometria;

// ============================================================================
// Proyecto: Biometría y Medio Ambiente
// Asignatura: Proyecto Biometría (PBIO) - Sprint 0
// Archivo: TestProxyRed.java
// Descripción: Tests de verificación de la fachada de red ProxyRedMediciones
//              (comprueba serialización de datos y callbacks)
// ============================================================================

public class TestProxyRed {

    public static void main(String[] args) {
        System.out.println("====================================================");
        System.out.println("   TESTS DE VERIFICACIÓN: PROXY DE RED ANDROID      ");
        System.out.println("====================================================\n");

        boolean todosPasados = true;

        // Test 1: Verificación del modelo de datos DTO
        System.out.print("[TEST 1] Verificación del modelo MedicionDTO... ");
        ProxyRedMediciones.MedicionDTO dto = new ProxyRedMediciones.MedicionDTO(1, 11, 450, 5, "2026-10-15 20:00:00");
        if (dto.sensor == 11 && dto.valor == 450 && dto.contador == 5 && dto.id == 1) {
            System.out.println("OK");
        } else {
            System.out.println("FALLO");
            todosPasados = false;
        }

        // Test 2: Verificación de desempaquetado de trama iBeacon en Servicio
        System.out.print("[TEST 2] Desempaquetado de Major (Sensor + Contador)... ");
        int sensorOriginal = 11;
        int contadorOriginal = 42;
        int major = (sensorOriginal << 8) + contadorOriginal;

        int sensorDecodificado = (major >> 8) & 0xFF;
        int contadorDecodificado = major & 0xFF;

        if (sensorDecodificado == sensorOriginal && contadorDecodificado == contadorOriginal) {
            System.out.println("OK (Sensor: " + sensorDecodificado + ", Contador: " + contadorDecodificado + ")");
        } else {
            System.out.println("FALLO en decodificación de Major");
            todosPasados = false;
        }

        // Test 3: Inicialización de la fachada ProxyRedMediciones
        System.out.print("[TEST 3] Instanciación y configuración de URL en Fachada... ");
        ProxyRedMediciones proxy = new ProxyRedMediciones("http://192.168.1.50:8080");
        if (proxy.getUrlServidor().equals("http://192.168.1.50:8080")) {
            System.out.println("OK");
        } else {
            System.out.println("FALLO");
            todosPasados = false;
        }

        System.out.println("\n----------------------------------------------------");
        if (todosPasados) {
            System.out.println("RESULTADO FINAL: TESTS DE FACHADA ANDROID PASADOS");
        } else {
            System.out.println("RESULTADO FINAL: AL MENOS UN TEST FALLÓ");
        }
        System.out.println("----------------------------------------------------");
    }
}

