package com.example.proyectofinal.dao;

import com.example.proyectofinal.modelos.Planta;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
    // Crear una planta
    // ---------------------------------------------------------
    public void createPlanta(Planta planta) {
        String newId = plantasRef.push().getKey();
        if (newId != null) {
            planta.id = newId;
            plantasRef.child(newId).setValue(planta);
        }
    }

    // ---------------------------------------------------------
    // Actualizar una planta
    // ---------------------------------------------------------
    public void updatePlanta(Planta planta) {
        plantasRef.child(planta.id).setValue(planta);
    }

    // ---------------------------------------------------------
    // Eliminar una planta
    // ---------------------------------------------------------
    public void removePlanta(String id) {
        plantasRef.child(id).removeValue();
    }

    // ---------------------------------------------------------
    // Filtrar lista de plantas por tipo (movido desde PlantaFragment)
    // ---------------------------------------------------------
    public List<Planta> filtrarPorTipo(List<Planta> plantas, String tipo) {
        if (tipo == null) return new ArrayList<>(plantas);
        List<Planta> resultado = new ArrayList<>();
        for (Planta p : plantas) {
            if (p.tipo != null && p.tipo.equals(tipo)) resultado.add(p);
        }
        return resultado;
    }
}