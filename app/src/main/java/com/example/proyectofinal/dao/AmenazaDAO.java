package com.example.proyectofinal.dao;

import com.example.proyectofinal.modelos.Amenaza;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class AmenazaDAO {

    private final DatabaseReference amenazasRef;

    public AmenazaDAO() {
        amenazasRef = FirebaseDatabase.getInstance().getReference("amenazas");
    }

    // ---------------------------------------------------------
    // Obtener todas las amenazas en tiempo real
    // ---------------------------------------------------------
    public void getAllAmenazas(ValueEventListener listener) {
        amenazasRef.addValueEventListener(listener);
    }

    // ---------------------------------------------------------
    // Obtener todas las amenazas (una sola vez)
    // ---------------------------------------------------------
    public void getAllAmenazasOnce(OnAmenazasLoadedCallback callback) {
        amenazasRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Amenaza> amenazas = new ArrayList<>();
                for (DataSnapshot amenazaSnap : snapshot.getChildren()) {
                    Amenaza amenaza = amenazaSnap.getValue(Amenaza.class);
                    if (amenaza != null) {
                        amenaza.setId(amenazaSnap.getKey());
                        amenazas.add(amenaza);
                    }
                }
                callback.onLoaded(amenazas);
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
        amenazasRef.removeEventListener(listener);
    }

    // ---------------------------------------------------------
    // Obtener una amenaza por ID
    // ---------------------------------------------------------
    public void getAmenazaById(String id, ValueEventListener listener) {
        amenazasRef.child(id).addListenerForSingleValueEvent(listener);
    }

    // ---------------------------------------------------------
    // Obtener amenaza por ID con callback
    // ---------------------------------------------------------
    public void getAmenazaById(String id, OnAmenazaLoadedCallback callback) {
        amenazasRef.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Amenaza amenaza = snapshot.getValue(Amenaza.class);
                if (amenaza != null) {
                    amenaza.setId(snapshot.getKey());
                    callback.onLoaded(amenaza);
                } else {
                    callback.onError(new Exception("Amenaza no encontrada"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.toException());
            }
        });
    }

    // ---------------------------------------------------------
    // Buscar amenazas por nombre
    // ---------------------------------------------------------
    public void searchAmenazasByNombre(String query, OnAmenazasLoadedCallback callback) {
        getAllAmenazasOnce(new OnAmenazasLoadedCallback() {
            @Override
            public void onLoaded(List<Amenaza> amenazas) {
                List<Amenaza> resultado = buscarPorNombre(amenazas, query);
                callback.onLoaded(resultado);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }

    // ---------------------------------------------------------
    // Obtener amenazas por tipo
    // ---------------------------------------------------------
    public void getAmenazasByTipo(String tipo, OnAmenazasLoadedCallback callback) {
        getAllAmenazasOnce(new OnAmenazasLoadedCallback() {
            @Override
            public void onLoaded(List<Amenaza> amenazas) {
                List<Amenaza> resultado = filtrarPorTipo(amenazas, tipo);
                callback.onLoaded(resultado);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }

    // ---------------------------------------------------------
    // Crear amenaza (solo admin)
    // ---------------------------------------------------------
    public void createAmenaza(Amenaza amenaza, OnOperationCallback callback) {
        String newId = amenazasRef.push().getKey();
        if (newId != null) {
            amenaza.setId(newId);
            amenazasRef.child(newId).setValue(amenaza)
                    .addOnSuccessListener(aVoid -> callback.onSuccess())
                    .addOnFailureListener(callback::onError);
        } else {
            callback.onError(new Exception("No se pudo generar ID"));
        }
    }

    // ---------------------------------------------------------
    // Actualizar amenaza (solo admin)
    // ---------------------------------------------------------
    public void updateAmenaza(Amenaza amenaza, OnOperationCallback callback) {
        amenazasRef.child(amenaza.getId()).setValue(amenaza)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    // ---------------------------------------------------------
    // Eliminar amenaza (solo admin)
    // ---------------------------------------------------------
    public void removeAmenaza(String id, OnOperationCallback callback) {
        amenazasRef.child(id).removeValue()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    // ---------------------------------------------------------
    // FILTROS Y BÚSQUEDAS
    // ---------------------------------------------------------

    /**
     * Filtrar lista de amenazas por tipo
     */
    public List<Amenaza> filtrarPorTipo(List<Amenaza> amenazas, String tipo) {
        if (tipo == null || tipo.isEmpty()) return new ArrayList<>(amenazas);

        List<Amenaza> resultado = new ArrayList<>();
        for (Amenaza a : amenazas) {
            if (a.getTipo() != null && a.getTipo().equalsIgnoreCase(tipo)) {
                resultado.add(a);
            }
        }
        return resultado;
    }

    /**
     * Buscar amenazas por nombre (parcial, case-insensitive)
     */
    public List<Amenaza> buscarPorNombre(List<Amenaza> amenazas, String query) {
        if (query == null || query.trim().isEmpty()) return new ArrayList<>(amenazas);

        String queryLower = query.toLowerCase().trim();
        List<Amenaza> resultado = new ArrayList<>();

        for (Amenaza a : amenazas) {
            if (a.getNombre() != null &&
                    a.getNombre().toLowerCase().contains(queryLower)) {
                resultado.add(a);
            }
        }
        return resultado;
    }

    /**
     * Filtrar amenazas que tienen tratamiento
     */
    public List<Amenaza> filtrarConTratamiento(List<Amenaza> amenazas) {
        List<Amenaza> resultado = new ArrayList<>();
        for (Amenaza a : amenazas) {
            if (a.tieneTratamiento()) {
                resultado.add(a);
            }
        }
        return resultado;
    }

    /**
     * Filtrar amenazas que tienen síntomas registrados
     */
    public List<Amenaza> filtrarConSintomas(List<Amenaza> amenazas) {
        List<Amenaza> resultado = new ArrayList<>();
        for (Amenaza a : amenazas) {
            if (a.tieneSintomas()) {
                resultado.add(a);
            }
        }
        return resultado;
    }

    /**
     * Ordenar amenazas alfabéticamente
     */
    public List<Amenaza> ordenarAlfabeticamente(List<Amenaza> amenazas) {
        List<Amenaza> resultado = new ArrayList<>(amenazas);
        resultado.sort((a1, a2) -> {
            String nombre1 = a1.getNombre() != null ? a1.getNombre() : "";
            String nombre2 = a2.getNombre() != null ? a2.getNombre() : "";
            return nombre1.compareToIgnoreCase(nombre2);
        });
        return resultado;
    }

    /**
     * Ordenar por tipo (plagas primero, luego enfermedades)
     */
    public List<Amenaza> ordenarPorTipo(List<Amenaza> amenazas) {
        List<Amenaza> resultado = new ArrayList<>(amenazas);
        resultado.sort((a1, a2) -> {
            String tipo1 = a1.getTipo() != null ? a1.getTipo() : "";
            String tipo2 = a2.getTipo() != null ? a2.getTipo() : "";
            return tipo1.compareToIgnoreCase(tipo2);
        });
        return resultado;
    }

    // ---------------------------------------------------------
    // Callbacks
    // ---------------------------------------------------------
    public interface OnAmenazasLoadedCallback {
        void onLoaded(List<Amenaza> amenazas);
        void onError(Exception e);
    }

    public interface OnAmenazaLoadedCallback {
        void onLoaded(Amenaza amenaza);
        void onError(Exception e);
    }

    public interface OnOperationCallback {
        void onSuccess();
        void onError(Exception e);
    }
}