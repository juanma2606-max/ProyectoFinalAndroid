package com.example.proyectofinal.dao;

import com.example.proyectofinal.modelos.Cultivo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CultivoDAO {

    private final FirebaseDatabase database;
    private final FirebaseAuth auth;

    public CultivoDAO() {
        database = FirebaseDatabase.getInstance();
        auth     = FirebaseAuth.getInstance();
    }

    // ---------------------------------------------------------
    // Ruta base: cultivos/{uid}/{huertoId}
    // ---------------------------------------------------------
    private DatabaseReference getCultivosRef(String huertoId) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return null;
        return database.getReference("cultivos")
                .child(user.getUid())
                .child(huertoId);
    }

    // ---------------------------------------------------------
    // Escucha en tiempo real los cultivos de un huerto
    // ---------------------------------------------------------
    public void getCultivosByHuerto(String huertoId, ValueEventListener listener) {
        DatabaseReference ref = getCultivosRef(huertoId);
        if (ref == null) return;
        ref.addValueEventListener(listener);
    }

    // ---------------------------------------------------------
    // Detener escucha en tiempo real
    // ---------------------------------------------------------
    public void removeListener(String huertoId, ValueEventListener listener) {
        DatabaseReference ref = getCultivosRef(huertoId);
        if (ref == null) return;
        ref.removeEventListener(listener);
    }

    // ---------------------------------------------------------
    // Crear un cultivo en un huerto
    // ---------------------------------------------------------
    public void createCultivo(String huertoId, Cultivo cultivo, OnCompleteCallback callback) {
        DatabaseReference ref = getCultivosRef(huertoId);
        if (ref == null) {
            callback.onError("Usuario no autenticado");
            return;
        }

        String fecha = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                .format(new Date());

        Map<String, Object> data = new HashMap<>();
        data.put("plantaId",     cultivo.plantaId);
        data.put("fechaSiembra", fecha);
        data.put("notas",        cultivo.notas != null ? cultivo.notas : "");
        data.put("estado",       cultivo.estado != null ? cultivo.estado : "creciendo");

        ref.push().setValue(data)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // ---------------------------------------------------------
    // Actualizar estado o notas de un cultivo
    // ---------------------------------------------------------
    public void updateCultivo(String huertoId, Cultivo cultivo, OnCompleteCallback callback) {
        DatabaseReference ref = getCultivosRef(huertoId);
        if (ref == null) {
            callback.onError("Usuario no autenticado");
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("notas",  cultivo.notas);
        data.put("estado", cultivo.estado);

        ref.child(cultivo.id).updateChildren(data)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // ---------------------------------------------------------
    // Eliminar un cultivo
    // ---------------------------------------------------------
    public void removeCultivo(String huertoId, String cultivoId, OnCompleteCallback callback) {
        DatabaseReference ref = getCultivosRef(huertoId);
        if (ref == null) {
            callback.onError("Usuario no autenticado");
            return;
        }

        ref.child(cultivoId).removeValue()
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // ---------------------------------------------------------
    // Callback genérico
    // ---------------------------------------------------------
    public interface OnCompleteCallback {
        void onSuccess();
        void onError(String message);
    }
}