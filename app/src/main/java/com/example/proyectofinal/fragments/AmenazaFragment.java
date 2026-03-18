package com.example.proyectofinal.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyectofinal.R;
import com.example.proyectofinal.activities.AmenazasDetallesActivity;
import com.example.proyectofinal.adaptadores.AmenazaAdapter;
import com.example.proyectofinal.dao.AmenazaDAO;
import com.example.proyectofinal.modelos.Amenaza;
import com.google.android.material.chip.Chip;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AmenazaFragment extends Fragment {

    private RecyclerView recycler;
    private AmenazaAdapter adapter;
    private final List<Amenaza> amenazas = new ArrayList<>();
    private final List<Amenaza> filtradas = new ArrayList<>();
    private AmenazaDAO amenazaDAO;
    private ValueEventListener amenazaListener;

    private String tipoActivo = "plaga"; // "plaga" o "enfermedad"

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_amenaza, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        amenazaDAO = new AmenazaDAO();

        recycler = view.findViewById(R.id.recyclerAmenazas);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new AmenazaAdapter(requireContext(), filtradas, amenaza -> {
            Intent i = new Intent(requireContext(), AmenazasDetallesActivity.class);
            i.putExtra("id", amenaza.id);
            startActivity(i);
        });

        recycler.setAdapter(adapter);

        cargarAmenazas();
        configurarFiltros(view);
    }

    private void cargarAmenazas() {
        amenazaListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                amenazas.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Amenaza a = child.getValue(Amenaza.class);
                    if (a != null) {
                        a.id = child.getKey();
                        amenazas.add(a);
                    }
                }
                // Delegamos el filtrado al DAO
                aplicarFiltro();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };

        amenazaDAO.getAllAmenazas(amenazaListener);
    }

    private void configurarFiltros(View view) {
        Chip chipPlaga      = view.findViewById(R.id.btnPlaga);
        Chip chipEnfermedad = view.findViewById(R.id.btnEnfermedad);

        chipPlaga.setOnClickListener(v -> {
            tipoActivo = "plaga";
            aplicarFiltro();
        });

        chipEnfermedad.setOnClickListener(v -> {
            tipoActivo = "enfermedad";
            aplicarFiltro();
        });
    }

    // El filtrado se delega al DAO, el fragment solo actualiza la UI
    private void aplicarFiltro() {
        filtradas.clear();
        filtradas.addAll(amenazaDAO.filtrarPorTipo(amenazas, tipoActivo));
        adapter.updateList(filtradas);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Limpiar listener para evitar memory leaks
        if (amenazaListener != null) {
            amenazaDAO.removeListener(amenazaListener);
        }
    }
}