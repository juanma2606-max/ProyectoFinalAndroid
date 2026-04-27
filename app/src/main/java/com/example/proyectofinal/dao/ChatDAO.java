package com.example.proyectofinal.dao;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.proyectofinal.modelos.Mensaje;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Call;
import okhttp3.Callback;

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
     * Enviar lista de mensajes al backend
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
                msgObj.put("content", m.getContent());
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
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "❌ Error de red: " + e.getMessage());
                    notifyError(listener, "Error de conexión: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        String errorBody = response.body() != null ? response.body().string() : "Sin detalle";
                        Log.e(TAG, "❌ Error HTTP " + response.code() + ": " + errorBody);
                        notifyError(listener, "Error del servidor");
                        return;
                    }

                    if (response.body() == null) {
                        notifyError(listener, "Respuesta vacía del servidor");
                        return;
                    }

                    try {
                        String responseBody = response.body().string();
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
}
