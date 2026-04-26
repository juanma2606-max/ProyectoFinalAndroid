package com.example.proyectofinal.dao;

import com.example.proyectofinal.modelos.Huerto;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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
        return database.getReference("usuarios")
                .child(user.getUid())
                .child("huertos");
    }

    private DatabaseReference getHuertosRefForUser(String uid) {
        return database.getReference("usuarios")
                .child(uid)
                .child("huertos");
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
    // Obtener todos los huertos con callback
    // ---------------------------------------------------------
    public void getAllHuertosOnce(OnHuertosLoadedCallback callback) {
        DatabaseReference ref = getHuertosRef();
        if (ref == null) {
            callback.onError(new Exception("Usuario no autenticado"));
            return;
        }

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Huerto> huertos = new ArrayList<>();
                for (DataSnapshot huertoSnap : snapshot.getChildren()) {
                    Huerto huerto = huertoSnap.getValue(Huerto.class);
                    if (huerto != null) {
                        huerto.setId(huertoSnap.getKey());
                        huertos.add(huerto);
                    }
                }
                callback.onLoaded(huertos);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.toException());
            }
        });
    }

    // ---------------------------------------------------------
    // Obtener huertos de otro usuario (para admin)
    // ---------------------------------------------------------
    public void getHuertosByUid(String uid, OnHuertosLoadedCallback callback) {
        DatabaseReference ref = getHuertosRefForUser(uid);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Huerto> huertos = new ArrayList<>();
                for (DataSnapshot huertoSnap : snapshot.getChildren()) {
                    Huerto huerto = huertoSnap.getValue(Huerto.class);
                    if (huerto != null) {
                        huerto.setId(huertoSnap.getKey());
                        huertos.add(huerto);
                    }
                }
                callback.onLoaded(huertos);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.toException());
            }
        });
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
    // Obtener huerto por ID con callback
    // ---------------------------------------------------------
    public void getHuertoById(String id, OnHuertoLoadedCallback callback) {
        DatabaseReference ref = getHuertosRef();
        if (ref == null) {
            callback.onError(new Exception("Usuario no autenticado"));
            return;
        }

        ref.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Huerto huerto = snapshot.getValue(Huerto.class);
                if (huerto != null) {
                    huerto.setId(snapshot.getKey());
                    callback.onLoaded(huerto);
                } else {
                    callback.onError(new Exception("Huerto no encontrado"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.toException());
            }
        });
    }

    // ---------------------------------------------------------
    // Obtener huerto de otro usuario (para admin)
    // ---------------------------------------------------------
    public void getHuertoByUidAndId(String uid, String huertoId, OnHuertoLoadedCallback callback) {
        DatabaseReference ref = getHuertosRefForUser(uid);

        ref.child(huertoId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Huerto huerto = snapshot.getValue(Huerto.class);
                if (huerto != null) {
                    huerto.setId(snapshot.getKey());
                    callback.onLoaded(huerto);
                } else {
                    callback.onError(new Exception("Huerto no encontrado"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.toException());
            }
        });
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

        String fecha = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                .format(new Date());

        Map<String, Object> data = new HashMap<>();
        data.put("nombre", huerto.getNombre());
        data.put("descripcion", huerto.getDescripcion());
        data.put("foto", huerto.getFoto());
        data.put("ubicacion", huerto.getUbicacion());
        data.put("superficie", huerto.getSuperficie());
        data.put("tipo_suelo", huerto.getTipoSuelo());
        data.put("horas_sol", huerto.getHorasSol());
        data.put("tiene_riego", huerto.isTieneRiego());
        data.put("notas", huerto.getNotas());
        data.put("fecha_creacion", fecha);

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
        data.put("nombre", huerto.getNombre());
        data.put("descripcion", huerto.getDescripcion());
        data.put("foto", huerto.getFoto());
        data.put("ubicacion", huerto.getUbicacion());
        data.put("superficie", huerto.getSuperficie());
        data.put("tipo_suelo", huerto.getTipoSuelo());
        data.put("horas_sol", huerto.getHorasSol());
        data.put("tiene_riego", huerto.isTieneRiego());
        data.put("notas", huerto.getNotas());

        ref.child(huerto.getId()).updateChildren(data)
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
    // Contar número de huertos
    // ---------------------------------------------------------
    public void getHuertosCount(OnCountCallback callback) {
        DatabaseReference ref = getHuertosRef();
        if (ref == null) {
            callback.onCount(0);
            return;
        }

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                callback.onCount((int) snapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onCount(0);
            }
        });
    }

    // ---------------------------------------------------------
    // Callbacks
    // ---------------------------------------------------------
    public interface OnCompleteCallback {
        void onSuccess();
        void onError(String message);
    }

    public interface OnHuertosLoadedCallback {
        void onLoaded(List<Huerto> huertos);
        void onError(Exception e);
    }

    public interface OnHuertoLoadedCallback {
        void onLoaded(Huerto huerto);
        void onError(Exception e);
    }

    public interface OnCountCallback {
        void onCount(int count);
    }
}