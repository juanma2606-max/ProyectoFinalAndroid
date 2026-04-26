package com.example.proyectofinal.dao;

import com.example.proyectofinal.modelos.Planta;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class PlantaDAO {

    private final DatabaseReference plantasRef;

    public PlantaDAO() {
        this.plantasRef = FirebaseDatabase.getInstance().getReference("plantas");
    }

    // ---------------------------------------------------------
    // Obtener todas las plantas
    // ---------------------------------------------------------
    public void getAllPlantas(ValueEventListener listener) {
        plantasRef.addValueEventListener(listener);
    }

    // ---------------------------------------------------------
    // Obtener todas las plantas (una sola vez)
    // ---------------------------------------------------------
    public void getAllPlantasOnce(OnPlantasLoadedCallback callback) {
        plantasRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Planta> plantas = new ArrayList<>();
                for (DataSnapshot plantaSnap : snapshot.getChildren()) {
                    Planta planta = plantaSnap.getValue(Planta.class);
                    if (planta != null) {
                        planta.setId(plantaSnap.getKey());
                        plantas.add(planta);
                    }
                }
                callback.onLoaded(plantas);
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
        plantasRef.removeEventListener(listener);
    }

    // ---------------------------------------------------------
    // Obtener una planta por ID
    // ---------------------------------------------------------
    public void getPlantaById(String id, ValueEventListener listener) {
        plantasRef.child(id).addListenerForSingleValueEvent(listener);
    }

    // ---------------------------------------------------------
    // Obtener una planta por ID con callback
    // ---------------------------------------------------------
    public void getPlantaById(String id, OnPlantaLoadedCallback callback) {
        plantasRef.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Planta planta = snapshot.getValue(Planta.class);
                if (planta != null) {
                    planta.setId(snapshot.getKey());
                    callback.onLoaded(planta);
                } else {
                    callback.onError(new Exception("Planta no encontrada"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.toException());
            }
        });
    }

    // ---------------------------------------------------------
    // Buscar plantas por nombre
    // ---------------------------------------------------------
    public void searchPlantasByNombre(String query, OnPlantasLoadedCallback callback) {
        getAllPlantasOnce(new OnPlantasLoadedCallback() {
            @Override
            public void onLoaded(List<Planta> plantas) {
                List<Planta> resultado = buscarPorNombre(plantas, query);
                callback.onLoaded(resultado);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }

    // ---------------------------------------------------------
    // Obtener plantas por tipo
    // ---------------------------------------------------------
    public void getPlantasByTipo(String tipo, OnPlantasLoadedCallback callback) {
        getAllPlantasOnce(new OnPlantasLoadedCallback() {
            @Override
            public void onLoaded(List<Planta> plantas) {
                List<Planta> resultado = filtrarPorTipo(plantas, tipo);
                callback.onLoaded(resultado);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }

    // ---------------------------------------------------------
    // Crear una planta
    // ---------------------------------------------------------
    public void createPlanta(Planta planta) {
        String newId = plantasRef.push().getKey();
        if (newId != null) {
            planta.setId(newId);
            plantasRef.child(newId).setValue(planta);
        }
    }

    // ---------------------------------------------------------
    // Crear planta con callback
    // ---------------------------------------------------------
    public void createPlanta(Planta planta, OnOperationCallback callback) {
        String newId = plantasRef.push().getKey();
        if (newId != null) {
            planta.setId(newId);
            plantasRef.child(newId).setValue(planta)
                    .addOnSuccessListener(aVoid -> callback.onSuccess())
                    .addOnFailureListener(callback::onError);
        } else {
            callback.onError(new Exception("No se pudo generar ID"));
        }
    }

    // ---------------------------------------------------------
    // Actualizar una planta
    // ---------------------------------------------------------
    public void updatePlanta(Planta planta) {
        plantasRef.child(planta.getId()).setValue(planta);
    }

    // ---------------------------------------------------------
    // Actualizar planta con callback
    // ---------------------------------------------------------
    public void updatePlanta(Planta planta, OnOperationCallback callback) {
        plantasRef.child(planta.getId()).setValue(planta)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    // ---------------------------------------------------------
    // Eliminar una planta
    // ---------------------------------------------------------
    public void removePlanta(String id) {
        plantasRef.child(id).removeValue();
    }

    // ---------------------------------------------------------
    // Eliminar planta con callback
    // ---------------------------------------------------------
    public void removePlanta(String id, OnOperationCallback callback) {
        plantasRef.child(id).removeValue()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    // ---------------------------------------------------------
    // FILTROS Y BÚSQUEDAS
    // ---------------------------------------------------------

    /**
     * Filtrar lista de plantas por tipo
     */
    public List<Planta> filtrarPorTipo(List<Planta> plantas, String tipo) {
        if (tipo == null || tipo.isEmpty()) return new ArrayList<>(plantas);

        List<Planta> resultado = new ArrayList<>();
        for (Planta p : plantas) {
            if (p.getTipo() != null && p.getTipo().equalsIgnoreCase(tipo)) {
                resultado.add(p);
            }
        }
        return resultado;
    }

    /**
     * Buscar plantas por nombre (parcial, case-insensitive)
     */
    public List<Planta> buscarPorNombre(List<Planta> plantas, String query) {
        if (query == null || query.trim().isEmpty()) return new ArrayList<>(plantas);

        String queryLower = query.toLowerCase().trim();
        List<Planta> resultado = new ArrayList<>();

        for (Planta p : plantas) {
            if (p.getNombre() != null &&
                    p.getNombre().toLowerCase().contains(queryLower)) {
                resultado.add(p);
            }
        }
        return resultado;
    }

    /**
     * Filtrar plantas por estación
     */
    public List<Planta> filtrarPorEstacion(List<Planta> plantas, String estacion) {
        if (estacion == null || estacion.isEmpty()) return new ArrayList<>(plantas);

        List<Planta> resultado = new ArrayList<>();
        for (Planta p : plantas) {
            if (p.getEstacion() != null &&
                    (p.getEstacion().contains(estacion) || p.getEstacion().equals("todo-año"))) {
                resultado.add(p);
            }
        }
        return resultado;
    }

    /**
     * Filtrar plantas por nivel de riego
     */
    public List<Planta> filtrarPorRiego(List<Planta> plantas, String nivelRiego) {
        if (nivelRiego == null || nivelRiego.isEmpty()) return new ArrayList<>(plantas);

        List<Planta> resultado = new ArrayList<>();
        for (Planta p : plantas) {
            if (p.getRiego() != null && p.getRiego().equalsIgnoreCase(nivelRiego)) {
                resultado.add(p);
            }
        }
        return resultado;
    }

    /**
     * Filtrar plantas por nivel de luz
     */
    public List<Planta> filtrarPorLuz(List<Planta> plantas, String nivelLuz) {
        if (nivelLuz == null || nivelLuz.isEmpty()) return new ArrayList<>(plantas);

        List<Planta> resultado = new ArrayList<>();
        for (Planta p : plantas) {
            if (p.getLuz() != null && p.getLuz().equalsIgnoreCase(nivelLuz)) {
                resultado.add(p);
            }
        }
        return resultado;
    }

    /**
     * Ordenar plantas por tiempo de crecimiento (menor a mayor)
     */
    public List<Planta> ordenarPorTiempoCrecimiento(List<Planta> plantas) {
        List<Planta> resultado = new ArrayList<>(plantas);
        resultado.sort((p1, p2) -> Integer.compare(
                p1.getTiempoCrecimiento(),
                p2.getTiempoCrecimiento()
        ));
        return resultado;
    }

    /**
     * Ordenar plantas alfabéticamente
     */
    public List<Planta> ordenarAlfabeticamente(List<Planta> plantas) {
        List<Planta> resultado = new ArrayList<>(plantas);
        resultado.sort((p1, p2) -> {
            String nombre1 = p1.getNombre() != null ? p1.getNombre() : "";
            String nombre2 = p2.getNombre() != null ? p2.getNombre() : "";
            return nombre1.compareToIgnoreCase(nombre2);
        });
        return resultado;
    }

    // ---------------------------------------------------------
    // Callbacks
    // ---------------------------------------------------------
    public interface OnPlantasLoadedCallback {
        void onLoaded(List<Planta> plantas);
        void onError(Exception e);
    }

    public interface OnPlantaLoadedCallback {
        void onLoaded(Planta planta);
        void onError(Exception e);
    }

    public interface OnOperationCallback {
        void onSuccess();
        void onError(Exception e);
    }
}