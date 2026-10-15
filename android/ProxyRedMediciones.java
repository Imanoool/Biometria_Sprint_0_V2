package org.jordi.biometria;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

// ============================================================================
// Proyecto: Biometría y Medio Ambiente
// Asignatura: Proyecto Biometría (PBIO) - Sprint 0
// Archivo: ProxyRedMediciones.java
// Descripción: Fachada / Cliente Proxy de red para Android que abstrae el acceso
//              a los servicios web REST del servidor para guardar y consultar mediciones.
//
// ----------------------------------------------------------------------------
// (sensor:Z, valor:Z, contador:N, tiempo:Texto) -> guardarMedicion() -> Callback(exito:Booleano)
// limite:N -> obtenerUltimasMediciones() -> Callback(mediciones:Lista<Medicion>)
// sensor:Z -> obtenerUltimaMedicion() -> Callback(medicion:Medicion)
// ----------------------------------------------------------------------------
// ============================================================================

public class ProxyRedMediciones {

    private static final String ETIQUETA_LOG = "ProxyRedMediciones";

    // URL base configurable del servidor REST
    // Nota: 10.0.2.2 es la IP del host desde el emulador de Android Studio
    private String urlServidor = "http://10.0.2.2:8080";

    // ------------------------------------------------------------------------
    // Callbacks para la capa superior (UI o Servicio en segundo plano)
    // ------------------------------------------------------------------------

    public interface CallbackGuardarMedicion {
        void alCompletar(boolean exito, String mensaje);
    }

    public interface CallbackObtenerMediciones {
        void alCompletar(boolean exito, List<MedicionDTO> mediciones, String error);
    }

    public interface CallbackObtenerUltimaMedicion {
        void alCompletar(boolean exito, MedicionDTO medicion, String error);
    }

    // ------------------------------------------------------------------------
    // Modelo de datos para transferir mediciones (DTO)
    // ------------------------------------------------------------------------
    public static class MedicionDTO {
        public int id;
        public int sensor;
        public int valor;
        public int contador;
        public String tiempo;

        public MedicionDTO(int id, int sensor, int valor, int contador, String tiempo) {
            this.id = id;
            this.sensor = sensor;
            this.valor = valor;
            this.contador = contador;
            this.tiempo = tiempo;
        }

        @Override
        public String toString() {
            return "MedicionDTO{id=" + id + ", sensor=" + sensor + ", valor=" + valor +
                    ", contador=" + contador + ", tiempo='" + tiempo + "'}";
        }
    }

    // ------------------------------------------------------------------------
    // Constructores
    // ------------------------------------------------------------------------
    public ProxyRedMediciones() {
        Log.d(ETIQUETA_LOG, "ProxyRedMediciones instanciado con URL: " + this.urlServidor);
    }

    public ProxyRedMediciones(String urlServidor) {
        this.urlServidor = urlServidor;
        Log.d(ETIQUETA_LOG, "ProxyRedMediciones instanciado con URL personalizada: " + this.urlServidor);
    }

    public void setUrlServidor(String urlServidor) {
        this.urlServidor = urlServidor;
    }

    public String getUrlServidor() {
        return this.urlServidor;
    }

    // ------------------------------------------------------------------------
    // (sensor:Z, valor:Z, contador:N) -> guardarMedicion() -> Callback(exito:Booleano)
    // ------------------------------------------------------------------------
    public void guardarMedicion(int sensor, int valor, int contador, final CallbackGuardarMedicion callback) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String tiempoActual = sdf.format(new Date());
        guardarMedicion(sensor, valor, contador, tiempoActual, callback);
    }

    // ------------------------------------------------------------------------
    // (sensor:Z, valor:Z, contador:N, tiempo:Texto) -> guardarMedicion() -> Callback(exito:Booleano)
    // ------------------------------------------------------------------------
    public void guardarMedicion(int sensor, int valor, int contador, String tiempo, final CallbackGuardarMedicion callback) {
        Log.d(ETIQUETA_LOG, "guardarMedicion(): sensor=" + sensor + ", valor=" + valor + ", contador=" + contador);

        try {
            JSONObject json = new JSONObject();
            json.put("sensor", sensor);
            json.put("valor", valor);
            json.put("contador", contador);
            json.put("tiempo", tiempo);

            String urlDestino = this.urlServidor + "/rest/guardarMedicion.php";
            PeticionarioREST peticionario = new PeticionarioREST();

            peticionario.hacerPeticionREST("POST", urlDestino, json.toString(), new PeticionarioREST.RespuestaREST() {
                @Override
                public void callback(int codigo, String cuerpo) {
                    Log.d(ETIQUETA_LOG, "guardarMedicion callback(): HTTP " + codigo + ", cuerpo=" + cuerpo);
                    if (codigo == 200 || codigo == 201) {
                        try {
                            JSONObject res = new JSONObject(cuerpo);
                            boolean ok = res.optBoolean("resultado", false);
                            if (callback != null) {
                                callback.alCompletar(ok, ok ? "Guardado con éxito" : "Error en el servidor");
                            }
                        } catch (JSONException e) {
                            if (callback != null) {
                                callback.alCompletar(false, "Respuesta JSON no válida: " + e.getMessage());
                            }
                        }
                    } else {
                        if (callback != null) {
                            callback.alCompletar(false, "Error HTTP " + codigo + ": " + cuerpo);
                        }
                    }
                }
            });

        } catch (JSONException e) {
            Log.e(ETIQUETA_LOG, "Error al serializar JSON de medición: " + e.getMessage());
            if (callback != null) {
                callback.alCompletar(false, "Error al crear JSON: " + e.getMessage());
            }
        }
    }

    // ------------------------------------------------------------------------
    // limite:N -> obtenerUltimasMediciones() -> Callback(mediciones:Lista<Medicion>)
    // ------------------------------------------------------------------------
    public void obtenerUltimasMediciones(int limite, final CallbackObtenerMediciones callback) {
        String urlDestino = this.urlServidor + "/rest/obtenerMediciones.php?limite=" + limite;
        Log.d(ETIQUETA_LOG, "obtenerUltimasMediciones(): URL=" + urlDestino);

        PeticionarioREST peticionario = new PeticionarioREST();
        peticionario.hacerPeticionREST("GET", urlDestino, null, new PeticionarioREST.RespuestaREST() {
            @Override
            public void callback(int codigo, String cuerpo) {
                Log.d(ETIQUETA_LOG, "obtenerUltimasMediciones callback(): HTTP " + codigo);
                if (codigo == 200) {
                    try {
                        JSONArray array = new JSONArray(cuerpo);
                        List<MedicionDTO> lista = new ArrayList<>();
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            MedicionDTO m = new MedicionDTO(
                                    obj.optInt("id", 0),
                                    obj.optInt("sensor", 0),
                                    obj.optInt("valor", 0),
                                    obj.optInt("contador", 0),
                                    obj.optString("tiempo", "")
                            );
                            lista.add(m);
                        }
                        if (callback != null) {
                            callback.alCompletar(true, lista, null);
                        }
                    } catch (JSONException e) {
                        if (callback != null) {
                            callback.alCompletar(false, null, "Error al parsear JSON: " + e.getMessage());
                        }
                    }
                } else {
                    if (callback != null) {
                        callback.alCompletar(false, null, "Error HTTP " + codigo + ": " + cuerpo);
                    }
                }
            }
        });
    }

    // ------------------------------------------------------------------------
    // sensor:Z -> obtenerUltimaMedicion() -> Callback(medicion:Medicion)
    // ------------------------------------------------------------------------
    public void obtenerUltimaMedicion(Integer sensor, final CallbackObtenerUltimaMedicion callback) {
        String urlDestino = this.urlServidor + "/rest/obtenerMediciones.php?ultima=true";
        if (sensor != null) {
            urlDestino += "&sensor=" + sensor;
        }

        Log.d(ETIQUETA_LOG, "obtenerUltimaMedicion(): URL=" + urlDestino);

        PeticionarioREST peticionario = new PeticionarioREST();
        peticionario.hacerPeticionREST("GET", urlDestino, null, new PeticionarioREST.RespuestaREST() {
            @Override
            public void callback(int codigo, String cuerpo) {
                Log.d(ETIQUETA_LOG, "obtenerUltimaMedicion callback(): HTTP " + codigo);
                if (codigo == 200) {
                    try {
                        JSONObject obj = new JSONObject(cuerpo);
                        MedicionDTO m = new MedicionDTO(
                                obj.optInt("id", 0),
                                obj.optInt("sensor", 0),
                                obj.optInt("valor", 0),
                                obj.optInt("contador", 0),
                                obj.optString("tiempo", "")
                        );
                        if (callback != null) {
                            callback.alCompletar(true, m, null);
                        }
                    } catch (JSONException e) {
                        if (callback != null) {
                            callback.alCompletar(false, null, "Error parseando medición: " + e.getMessage());
                        }
                    }
                } else {
                    if (callback != null) {
                        callback.alCompletar(false, null, "Error HTTP " + codigo + ": " + cuerpo);
                    }
                }
            }
        });
    }
}

