package com.example.proyectofinal.dao;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.proyectofinal.modelos.Mensaje;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class ChatDAO {

    private static final String TAG = "ChatDAO";
    private static final String API_URL = "https://huerting-backend.onrender.com/api/chat";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final OkHttpClient client;
    private final Handler mainHandler;

    public interface OnChatResponseListener {
        void onSuccess(String response);
        void onError(String message);
    }

    public ChatDAO() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * Enviar lista de mensajes al backend.
     * Si un mensaje tiene imagen (tieneImagen() == true), su "content" se envía
     * como array multimodal [{type: "text", ...}, {type: "image_url", ...}],
     * que es el formato que espera Grok para analizar imágenes.
     */
    public void enviarMensaje(List<Mensaje> mensajes, OnChatResponseListener listener) {

        if (mensajes == null || mensajes.isEmpty()) {
            notifyError(listener, "Lista de mensajes vacía");
            return;
        }

        try {
            // Construir JSON
            JSONObject payload = new JSONObject();
            JSONArray messagesArray = new JSONArray();

            for (Mensaje m : mensajes) {
                JSONObject msgObj = new JSONObject();
                msgObj.put("role", m.getRole());

                if (m.tieneImagen()) {
                    JSONArray contentArray = new JSONArray();

                    // Parte de texto
                    JSONObject textPart = new JSONObject();
                    textPart.put("type", "text");
                    textPart.put("text", m.getContent());
                    contentArray.put(textPart);

                    // Parte de imagen
                    JSONObject imagePart = new JSONObject();
                    imagePart.put("type", "image_url");
                    JSONObject imageUrlObj = new JSONObject();
                    imageUrlObj.put("url", m.getImageUrl());
                    imagePart.put("image_url", imageUrlObj);
                    contentArray.put(imagePart);

                    msgObj.put("content", contentArray);
                } else {
                    msgObj.put("content", m.getContent());
                }

                messagesArray.put(msgObj);
            }

            payload.put("messages", messagesArray);

            Log.d(TAG, "📤 Payload: " + payload);

            // Crear request
            RequestBody body = RequestBody.create(JSON, payload.toString());
            Request request = new Request.Builder()
                    .url(API_URL)
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .build();

            // Ejecutar async
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e(TAG, "❌ Error de red: " + e.getMessage());
                    notifyError(listener, "Error de conexión: " + e.getMessage());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    try (ResponseBody responseBodyObj = response.body()) {
                        if (!response.isSuccessful()) {
                            String errorBody = responseBodyObj != null ? responseBodyObj.string() : "Sin detalle";
                            Log.e(TAG, "❌ Error HTTP " + response.code() + ": " + errorBody);
                            notifyError(listener, "Error del servidor");
                            return;
                        }

                        if (responseBodyObj == null) {
                            notifyError(listener, "Respuesta vacía");
                            return;
                        }

                        String responseBody = responseBodyObj.string();
                        Log.d(TAG, "✅ Respuesta recibida: " + responseBody);

                        JSONObject jsonResponse = new JSONObject(responseBody);
                        String content = jsonResponse.getString("content");

                        notifySuccess(listener, content);

                    } catch (Exception e) {
                        Log.e(TAG, "❌ Error parseando JSON: " + e.getMessage());
                        notifyError(listener, "Error procesando respuesta");
                    }
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "❌ Error construyendo payload: " + e.getMessage());
            notifyError(listener, "Error al preparar mensaje");
        }
    }

    /**
     * Notificar éxito en hilo principal
     */
    private void notifySuccess(OnChatResponseListener listener, String response) {
        mainHandler.post(() -> listener.onSuccess(response));
    }

    /**
     * Notificar error en hilo principal
     */
    private void notifyError(OnChatResponseListener listener, String message) {
        mainHandler.post(() -> listener.onError(message));
    }

    /**
     * Analizar huerto específico con contexto del usuario
     */
    public void analizarHuerto(String prompt, OnChatResponseListener listener) {

        if (prompt == null || prompt.trim().isEmpty()) {
            notifyError(listener, "Prompt vacío");
            return;
        }

        try {
            JSONObject payload = new JSONObject();
            payload.put("prompt", prompt);

            Log.d(TAG, "🌱 Analizando huerto...");

            RequestBody body = RequestBody.create(JSON, payload.toString());
            Request request = new Request.Builder()
                    .url("https://huerting-backend.onrender.com/api/analizar-huerto")
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e(TAG, "❌ Error análisis: " + e.getMessage());
                    notifyError(listener, "Error de conexión");
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    try (ResponseBody responseBodyObj = response.body()) {
                        if (!response.isSuccessful()) {
                            Log.e(TAG, "❌ Error HTTP " + response.code());
                            notifyError(listener, "Error del servidor");
                            return;
                        }

                        if (responseBodyObj == null) {
                            notifyError(listener, "Respuesta vacía");
                            return;
                        }

                        String responseBody = responseBodyObj.string();
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        String analisis = jsonResponse.getString("analisis");

                        Log.d(TAG, "✅ Análisis completado");
                        notifySuccess(listener, analisis);

                    } catch (Exception e) {
                        Log.e(TAG, "❌ Error parseando: " + e.getMessage());
                        notifyError(listener, "Error procesando análisis");
                    }
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "❌ Error construyendo request: " + e.getMessage());
            notifyError(listener, "Error preparando análisis");
        }
    }
}