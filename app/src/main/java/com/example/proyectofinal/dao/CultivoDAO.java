package com.example.proyectofinal.dao;

import com.example.proyectofinal.modelos.Cultivo;
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

public class CultivoDAO {

    private final FirebaseDatabase database;
    private final FirebaseAuth auth;

    public CultivoDAO() {
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    private DatabaseReference getCultivosRef(String huertoId) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return null;
        return database.getReference("users")
                .child(user.getUid())
                .child("huertos")
                .child(huertoId)
                .child("cultivos");
    }

    private DatabaseReference getCultivosRefForUser(String uid, String huertoId) {
        return database.getReference("users")
                .child(uid)
                .child("huertos")
                .child(huertoId)
                .child("cultivos");
    }

    public void getCultivosByHuerto(String huertoId, ValueEventListener listener) {
        DatabaseReference ref = getCultivosRef(huertoId);
        if (ref == null) return;
        ref.addValueEventListener(listener);
    }

    public void getCultivosByHuertoOnce(String huertoId, OnCultivosLoadedCallback callback) {
        DatabaseReference ref = getCultivosRef(huertoId);
        if (ref == null) {
            callback.onError(new Exception("Usuario no autenticado"));
            return;
        }

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Cultivo> cultivos = new ArrayList<>();
                for (DataSnapshot cultivoSnap : snapshot.getChildren()) {
                    Cultivo cultivo = cultivoSnap.getValue(Cultivo.class);
                    if (cultivo != null) {
                        cultivo.setId(cultivoSnap.getKey());
                        cultivos.add(cultivo);
                    }
                }
                callback.onLoaded(cultivos);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.toException());
            }
        });
    }

    public void getCultivosByUserAndHuerto(String uid, String huertoId,
                                           OnCultivosLoadedCallback callback) {
        DatabaseReference ref = getCultivosRefForUser(uid, huertoId);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Cultivo> cultivos = new ArrayList<>();
                for (DataSnapshot cultivoSnap : snapshot.getChildren()) {
                    Cultivo cultivo = cultivoSnap.getValue(Cultivo.class);
                    if (cultivo != null) {
                        cultivo.setId(cultivoSnap.getKey());
                        cultivos.add(cultivo);
                    }
                }
                callback.onLoaded(cultivos);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.toException());
            }
        });
    }

    // ---------------------------------------------------------
    // ADMIN: cultivos de un huerto de otro usuario (tiempo real)
    // ---------------------------------------------------------
    public void getCultivosByHuertoForUser(String uid, String huertoId, ValueEventListener listener) {
        getCultivosRefForUser(uid, huertoId).addValueEventListener(listener);
    }

    public void removeListenerForUser(String uid, String huertoId, ValueEventListener listener) {
        getCultivosRefForUser(uid, huertoId).removeEventListener(listener);
    }

    public void getCultivoByIdForUser(String uid, String huertoId, String cultivoId, OnCultivoLoadedCallback callback) {
        DatabaseReference ref = getCultivosRefForUser(uid, huertoId);

        ref.child(cultivoId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Cultivo cultivo = snapshot.getValue(Cultivo.class);
                if (cultivo != null) {
                    cultivo.setId(snapshot.getKey());
                    callback.onLoaded(cultivo);
                } else {
                    callback.onError(new Exception("Cultivo no encontrado"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.toException());
            }
        });
    }

    public void getCultivoById(String huertoId, String cultivoId, OnCultivoLoadedCallback callback) {
        DatabaseReference ref = getCultivosRef(huertoId);
        if (ref == null) {
            callback.onError(new Exception("Usuario no autenticado"));
            return;
        }

        ref.child(cultivoId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Cultivo cultivo = snapshot.getValue(Cultivo.class);
                if (cultivo != null) {
                    cultivo.setId(snapshot.getKey());
                    callback.onLoaded(cultivo);
                } else {
                    callback.onError(new Exception("Cultivo no encontrado"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.toException());
            }
        });
    }

    public void removeListener(String huertoId, ValueEventListener listener) {
        DatabaseReference ref = getCultivosRef(huertoId);
        if (ref == null) return;
        ref.removeEventListener(listener);
    }

    public void createCultivo(String huertoId, Cultivo cultivo, OnCompleteCallback callback) {
        DatabaseReference ref = getCultivosRef(huertoId);
        if (ref == null) {
            callback.onError("Usuario no autenticado");
            return;
        }

        crearCultivoEn(ref, cultivo, callback);
    }

    // ---------------------------------------------------------
    // ADMIN: crear cultivo en el huerto de otro usuario
    // ---------------------------------------------------------
    public void createCultivoForUser(String uid, String huertoId, Cultivo cultivo, OnCompleteCallback callback) {
        DatabaseReference ref = getCultivosRefForUser(uid, huertoId);
        crearCultivoEn(ref, cultivo, callback);
    }

    private void crearCultivoEn(DatabaseReference ref, Cultivo cultivo, OnCompleteCallback callback) {
        String fecha = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                .format(new Date());

        Map<String, Object> data = new HashMap<>();
        data.put("nombre", cultivo.getNombre());
        data.put("plantaId", cultivo.getPlantaId());
        data.put("fecha_siembra", fecha);
        data.put("cantidad", cultivo.getCantidad());
        data.put("estado", cultivo.getEstado() != null ? cultivo.getEstado() : "plantado");
        data.put("notas", cultivo.getNotas() != null ? cultivo.getNotas() : "");

        if (cultivo.getAmenazaId() != null && !cultivo.getAmenazaId().isEmpty()) {
            data.put("amenazaId", cultivo.getAmenazaId());
        }

        ref.push().setValue(data)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void updateCultivo(String huertoId, Cultivo cultivo, OnCompleteCallback callback) {
        DatabaseReference ref = getCultivosRef(huertoId);
        if (ref == null) {
            callback.onError("Usuario no autenticado");
            return;
        }

        actualizarCultivoEn(ref, cultivo, callback);
    }

    // ---------------------------------------------------------
    // ADMIN: actualizar cultivo en el huerto de otro usuario
    // ---------------------------------------------------------
    public void updateCultivoForUser(String uid, String huertoId, Cultivo cultivo, OnCompleteCallback callback) {
        DatabaseReference ref = getCultivosRefForUser(uid, huertoId);
        actualizarCultivoEn(ref, cultivo, callback);
    }

    private void actualizarCultivoEn(DatabaseReference ref, Cultivo cultivo, OnCompleteCallback callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("nombre", cultivo.getNombre());
        data.put("cantidad", cultivo.getCantidad());
        data.put("estado", cultivo.getEstado());
        data.put("notas", cultivo.getNotas());

        if (cultivo.getAmenazaId() != null && !cultivo.getAmenazaId().isEmpty()) {
            data.put("amenazaId", cultivo.getAmenazaId());
        } else {
            data.put("amenazaId", null);
        }

        ref.child(cultivo.getId()).updateChildren(data)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void updateEstado(String huertoId, String cultivoId, String nuevoEstado,
                             OnCompleteCallback callback) {
        DatabaseReference ref = getCultivosRef(huertoId);
        if (ref == null) {
            callback.onError("Usuario no autenticado");
            return;
        }

        ref.child(cultivoId).child("estado").setValue(nuevoEstado)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void marcarEnfermo(String huertoId, String cultivoId, String amenazaId,
                              OnCompleteCallback callback) {
        DatabaseReference ref = getCultivosRef(huertoId);
        if (ref == null) {
            callback.onError("Usuario no autenticado");
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("estado", "enfermo");
        data.put("amenazaId", amenazaId);

        ref.child(cultivoId).updateChildren(data)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void curarCultivo(String huertoId, String cultivoId, OnCompleteCallback callback) {
        DatabaseReference ref = getCultivosRef(huertoId);
        if (ref == null) {
            callback.onError("Usuario no autenticado");
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("estado", "creciendo");
        data.put("amenazaId", null);

        ref.child(cultivoId).updateChildren(data)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

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
    // ADMIN: eliminar cultivo en el huerto de otro usuario
    // ---------------------------------------------------------
    public void removeCultivoForUser(String uid, String huertoId, String cultivoId, OnCompleteCallback callback) {
        DatabaseReference ref = getCultivosRefForUser(uid, huertoId);

        ref.child(cultivoId).removeValue()
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public List<Cultivo> filtrarPorEstado(List<Cultivo> cultivos, String estado) {
        if (estado == null || estado.isEmpty()) return new ArrayList<>(cultivos);

        List<Cultivo> resultado = new ArrayList<>();
        for (Cultivo c : cultivos) {
            if (c.getEstado() != null && c.getEstado().equalsIgnoreCase(estado)) {
                resultado.add(c);
            }
        }
        return resultado;
    }

    public List<Cultivo> getCultivosActivos(List<Cultivo> cultivos) {
        List<Cultivo> resultado = new ArrayList<>();
        for (Cultivo c : cultivos) {
            if (!c.estaCosechado()) {
                resultado.add(c);
            }
        }
        return resultado;
    }

    public List<Cultivo> getCultivosEnfermos(List<Cultivo> cultivos) {
        List<Cultivo> resultado = new ArrayList<>();
        for (Cultivo c : cultivos) {
            if (c.estaEnfermo()) {
                resultado.add(c);
            }
        }
        return resultado;
    }

    public interface OnCompleteCallback {
        void onSuccess();
        void onError(String message);
    }

    public interface OnCultivosLoadedCallback {
        void onLoaded(List<Cultivo> cultivos);
        void onError(Exception e);
    }

    public interface OnCultivoLoadedCallback {
        void onLoaded(Cultivo cultivo);
        void onError(Exception e);
    }
}