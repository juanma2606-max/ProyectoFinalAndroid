package com.example.proyectofinal.utils;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

/**
 * Utilidad para subir imágenes directamente a Cloudinary desde la app,
 * usando un "unsigned upload preset" (no requiere exponer el API secret).
 *
 * CONFIGURACIÓN NECESARIA (una sola vez en tu cuenta de Cloudinary):
 *   1. Crea una cuenta gratuita en https://cloudinary.com
 *   2. Copia tu "Cloud name" (Dashboard) y pégalo en CLOUD_NAME.
 *   3. Ve a Settings -> Upload -> Upload presets -> Add upload preset.
 *      - Signing Mode: "Unsigned"
 *      - (Opcional) Folder: "huerting" para organizar las imágenes.
 *   4. Copia el nombre del preset y pégalo en UPLOAD_PRESET.
 */
public class CloudinaryUploader {

    // TODO: sustituir por los valores de tu cuenta de Cloudinary
    private static final String CLOUD_NAME = "dedqzjwq3";
    private static final String UPLOAD_PRESET = "Huerting";

    private static final String UPLOAD_URL =
            "https://api.cloudinary.com/v1_1/" + CLOUD_NAME + "/image/upload";

    public interface OnUploadResult {
        void onSuccess(String secureUrl);
        void onError(String mensaje);
    }

    /**
     * Sube la imagen indicada por el Uri (seleccionada desde galería) a Cloudinary.
     * El callback se ejecuta siempre en el hilo principal.
     */
    public static void subirImagen(Context context, Uri imagenUri, OnUploadResult callback) {
        Context appContext = context.getApplicationContext();
        Handler mainHandler = new Handler(Looper.getMainLooper());

        new Thread(() -> {
            try {
                byte[] datos = leerBytes(appContext, imagenUri);
                if (datos == null || datos.length == 0) {
                    throw new Exception("No se pudo leer la imagen seleccionada");
                }

                String secureUrl = enviarACloudinary(datos);
                mainHandler.post(() -> callback.onSuccess(secureUrl));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e.getMessage() != null ? e.getMessage() : "Error desconocido"));
            }
        }).start();
    }

    // ---------------------------------------------------------
    // Lee el contenido del Uri en un array de bytes
    // ---------------------------------------------------------
    private static byte[] leerBytes(Context context, Uri uri) throws Exception {
        try (InputStream input = context.getContentResolver().openInputStream(uri)) {
            if (input == null) return null;

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] chunk = new byte[8192];
            int leidos;
            while ((leidos = input.read(chunk)) != -1) {
                buffer.write(chunk, 0, leidos);
            }
            return buffer.toByteArray();
        }
    }

    // ---------------------------------------------------------
    // Construye y envía la petición multipart/form-data a Cloudinary
    // ---------------------------------------------------------
    private static String enviarACloudinary(byte[] imagenBytes) throws Exception {
        String boundary = "----HuertingBoundary" + UUID.randomUUID();
        String lineEnd = "\r\n";
        String twoHyphens = "--";

        URL url = new URL(UPLOAD_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setUseCaches(false);
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Connection", "Keep-Alive");
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(30000);

        try (OutputStream out = conn.getOutputStream()) {
            // Campo: upload_preset
            escribirCampoTexto(out, boundary, lineEnd, twoHyphens, "upload_preset", UPLOAD_PRESET);

            // Campo: file (la imagen)
            out.write((twoHyphens + boundary + lineEnd).getBytes());
            out.write(("Content-Disposition: form-data; name=\"file\"; filename=\""
                    + "imagen_" + System.currentTimeMillis() + ".jpg\"" + lineEnd).getBytes());
            out.write(("Content-Type: image/jpeg" + lineEnd).getBytes());
            out.write(lineEnd.getBytes());
            out.write(imagenBytes);
            out.write(lineEnd.getBytes());

            // Fin del multipart
            out.write((twoHyphens + boundary + twoHyphens + lineEnd).getBytes());
        }

        int codigo = conn.getResponseCode();
        InputStream respuestaStream = (codigo >= 200 && codigo < 300)
                ? conn.getInputStream()
                : conn.getErrorStream();

        String respuesta = new String(leerStream(respuestaStream));
        conn.disconnect();

        JSONObject json = new JSONObject(respuesta);

        if (codigo >= 200 && codigo < 300 && json.has("secure_url")) {
            return json.getString("secure_url");
        } else {
            String mensajeError = json.has("error")
                    ? json.getJSONObject("error").optString("message", "Error al subir la imagen")
                    : "Error al subir la imagen (código " + codigo + ")";
            throw new Exception(mensajeError);
        }
    }

    private static void escribirCampoTexto(OutputStream out, String boundary, String lineEnd,
                                           String twoHyphens, String nombre, String valor) throws Exception {
        out.write((twoHyphens + boundary + lineEnd).getBytes());
        out.write(("Content-Disposition: form-data; name=\"" + nombre + "\"" + lineEnd).getBytes());
        out.write(lineEnd.getBytes());
        out.write(valor.getBytes());
        out.write(lineEnd.getBytes());
    }

    private static byte[] leerStream(InputStream input) throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] chunk = new byte[4096];
        int leidos;
        while ((leidos = input.read(chunk)) != -1) {
            buffer.write(chunk, 0, leidos);
        }
        return buffer.toByteArray();
    }
}