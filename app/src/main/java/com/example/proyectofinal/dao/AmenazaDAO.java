package com.example.proyectofinal.dao;

import com.example.proyectofinal.modelos.Amenaza;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AmenazaDAO {

    private final DatabaseReference ref;

    public AmenazaDAO() {
        ref = FirebaseDatabase.getInstance().getReference("amenazas");
    }

    // ---------------------------------------------------------
    // Obtener todas las amenazas en tiempo real
    // ---------------------------------------------------------
    public void getAllAmenazas(ValueEventListener listener) {
        ref.addValueEventListener(listener);
    }

    // ---------------------------------------------------------
    // Detener escucha en tiempo real
    // ---------------------------------------------------------
    public void removeListener(ValueEventListener listener) {
        ref.removeEventListener(listener);
    }

    // ---------------------------------------------------------
    // Obtener una amenaza por ID
    // ---------------------------------------------------------
    public void getAmenazaById(String id, ValueEventListener listener) {
        ref.child(id).addListenerForSingleValueEvent(listener);
    }

    // ---------------------------------------------------------
    // Filtrar lista de amenazas por tipo (movido desde AmenazaFragment)
    // ---------------------------------------------------------
    public List<Amenaza> filtrarPorTipo(List<Amenaza> amenazas, String tipo) {
        List<Amenaza> resultado = new ArrayList<>();
        for (Amenaza a : amenazas) {
            if (a.tipo != null && a.tipo.equals(tipo)) resultado.add(a);
        }
        return resultado;
    }
}