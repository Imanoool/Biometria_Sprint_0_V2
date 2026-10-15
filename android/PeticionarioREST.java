package org.jordi.biometria;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import android.os.AsyncTask;
import android.util.Log;

// ------------------------------------------------------------------------
// Asignatura: Proyecto Biometría (PBIO) - Sprint 0
// Archivo: PeticionarioREST.java
// Descripción: Cliente HTTP asíncrono para peticiones REST en Android
// ------------------------------------------------------------------------
public class PeticionarioREST extends AsyncTask<Void, Void, Boolean> {

    // --------------------------------------------------------------------
    // Interfaz de callback para respuestas REST
    // --------------------------------------------------------------------
    public interface RespuestaREST {
        void callback (int codigo, String cuerpo);
    }

    private String elMetodo;
    private String urlDestino;
    private String elCuerpo = null;
    private RespuestaREST laRespuesta;

    private int codigoRespuesta;
    private String cuerpoRespuesta = "";

    // --------------------------------------------------------------------
    // metodo:Texto, urlDestino:Texto, cuerpo:Texto, laRespuesta:Callback -> hacerPeticionREST()
    // --------------------------------------------------------------------
    public void hacerPeticionREST (String metodo, String urlDestino, String cuerpo, RespuestaREST laRespuesta) {
        this.elMetodo = metodo;
        this.urlDestino = urlDestino;
        this.elCuerpo = cuerpo;
        this.laRespuesta = laRespuesta;

        this.execute(); // Otro thread ejecuta doInBackground()
    }

    public PeticionarioREST() {
        Log.d("PeticionarioREST", "constructor()");
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        Log.d("PeticionarioREST", "doInBackground(): conectando a >" + urlDestino + "< con método " + elMetodo);

        try {
            URL url = new URL(urlDestino);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestMethod(this.elMetodo);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setDoInput(true);

            if (!this.elMetodo.equals("GET") && this.elCuerpo != null) {
                connection.setDoOutput(true);
                DataOutputStream dos = new DataOutputStream(connection.getOutputStream());
                byte[] bytesCuerpo = this.elCuerpo.getBytes("UTF-8");
                dos.write(bytesCuerpo);
                dos.flush();
                dos.close();
            }

            this.codigoRespuesta = connection.getResponseCode();
            Log.d("PeticionarioREST", "doInBackground() código respuesta = " + this.codigoRespuesta);

            try {
                InputStream is = (this.codigoRespuesta >= 400) 
                    ? connection.getErrorStream() 
                    : connection.getInputStream();

                if (is != null) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                    StringBuilder acumulador = new StringBuilder();
                    String linea;
                    while ((linea = br.readLine()) != null) {
                        acumulador.append(linea);
                    }
                    this.cuerpoRespuesta = acumulador.toString();
                    br.close();
                }
                connection.disconnect();
            } catch (IOException ex) {
                Log.d("PeticionarioREST", "doInBackground(): sin cuerpo o error al leer stream: " + ex.getMessage());
            }

            return true;
        } catch (Exception ex) {
            Log.d("PeticionarioREST", "doInBackground(): excepción: " + ex.getMessage());
            this.codigoRespuesta = 0;
            this.cuerpoRespuesta = ex.getMessage();
        }

        return false;
    }

    @Override
    protected void onPostExecute(Boolean comoFue) {
        Log.d("PeticionarioREST", "onPostExecute() éxito = " + comoFue + ", código = " + this.codigoRespuesta);
        if (this.laRespuesta != null) {
            this.laRespuesta.callback(this.codigoRespuesta, this.cuerpoRespuesta);
        }
    }
}

