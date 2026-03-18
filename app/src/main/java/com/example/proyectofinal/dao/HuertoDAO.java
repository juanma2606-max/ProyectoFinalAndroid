package com.example.proyectofinal.dao;

import com.example.proyectofinal.modelos.Huerto;
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

public class HuertoDAO {

    private final FirebaseDatabase database;
    private final FirebaseAuth auth;

    public HuertoDAO() {
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    // ---------------------------------------------------------
    // Usuario autenticado actual
    // ---------------------------------------------------------
    private FirebaseUser getUser() {
        return auth.getCurrentUser();
    }

    private DatabaseReference getHuertosRef() {
        FirebaseUser user = getUser();
        if (user == null) return null;
        return database.getReference("huertos").child(user.getUid());
    }

    // ---------------------------------------------------------
    // Escucha en tiempo real todos los huertos del usuario
    // ---------------------------------------------------------
    public void getAllHuertos(ValueEventListener listener) {
        DatabaseReference ref = getHuertosRef();
        if (ref == null) return;
        ref.addValueEventListener(listener);
    }

    // ---------------------------------------------------------
    // Detener escucha en tiempo real
    // ---------------------------------------------------------
    public void removeListener(ValueEventListener listener) {
        DatabaseReference ref = getHuertosRef();
        if (ref == null) return;
        ref.removeEventListener(listener);
    }

    // ---------------------------------------------------------
    // Obtener un huerto por ID
    // ---------------------------------------------------------
    public void getHuertoById(String id, ValueEventListener listener) {
        DatabaseReference ref = getHuertosRef();
        if (ref == null) return;
        ref.child(id).addListenerForSingleValueEvent(listener);
    }

    // ---------------------------------------------------------
    // Crear un nuevo huerto
    // ---------------------------------------------------------
    public void createHuerto(Huerto huerto, OnCompleteCallback callback) {
        DatabaseReference ref = getHuertosRef();
        if (ref == null) {
            callback.onError("Usuario no autenticado");
            return;
        }

        String fecha = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                .format(new Date());

        Map<String, Object> data = new HashMap<>();
        data.put("nombre", huerto.nombre);
        data.put("descripcion", huerto.descripcion);
        data.put("tipo", huerto.tipo);
        data.put("fechaInicio", fecha);

        ref.push().setValue(data)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // ---------------------------------------------------------
    // Actualizar un huerto existente
    // ---------------------------------------------------------
    public void updateHuerto(Huerto huerto, OnCompleteCallback callback) {
        DatabaseReference ref = getHuertosRef();
        if (ref == null) {
            callback.onError("Usuario no autenticado");
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("nombre", huerto.nombre);
        data.put("descripcion", huerto.descripcion);
        data.put("tipo", huerto.tipo);

        ref.child(huerto.id).updateChildren(data)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // ---------------------------------------------------------
    // Eliminar un huerto
    // ---------------------------------------------------------
    public void removeHuerto(String id, OnCompleteCallback callback) {
        DatabaseReference ref = getHuertosRef();
        if (ref == null) {
            callback.onError("Usuario no autenticado");
            return;
        }

        ref.child(id).removeValue()
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // ---------------------------------------------------------
    // Callback generico
    // ---------------------------------------------------------
    public interface OnCompleteCallback {
        void onSuccess();
        void onError(String message);
    }
}