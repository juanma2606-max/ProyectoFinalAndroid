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
import com.example.proyectofinal.activities.PlantaDetalleActivity;
import com.example.proyectofinal.adaptadores.PlantaAdapter;
import com.example.proyectofinal.dao.PlantaDAO;
import com.example.proyectofinal.modelos.Planta;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PlantaFragment extends Fragment {

    private RecyclerView recycler;
    private PlantaAdapter adapter;
    private final List<Planta> plantas = new ArrayList<>();
    private final List<Planta> filtradas = new ArrayList<>();
    private PlantaDAO plantaDAO;
    private ValueEventListener plantaListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_plantas, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        plantaDAO = new PlantaDAO();

        recycler = view.findViewById(R.id.recyclerPlantas);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new PlantaAdapter(requireContext(), filtradas, planta -> {
            Intent i = new Intent(requireContext(), PlantaDetalleActivity.class);
            i.putExtra("id", planta.id);
            startActivity(i);
        });

        recycler.setAdapter(adapter);

        cargarPlantas();
        configurarFiltros(view);
    }

    private void cargarPlantas() {
        plantaListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                plantas.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Planta p = child.getValue(Planta.class);
                    if (p != null) {
                        p.id = child.getKey();
                        plantas.add(p);
                    }
                }
                // Delegamos el filtrado al DAO
                filtradas.clear();
                filtradas.addAll(plantaDAO.filtrarPorTipo(plantas, null));
                adapter.updateList(filtradas);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };

        plantaDAO.getAllPlantas(plantaListener);
    }

    private void configurarFiltros(View view) {
        view.findViewById(R.id.btnTodas).setOnClickListener(v    -> aplicarFiltro(null));
        view.findViewById(R.id.btnHortaliza).setOnClickListener(v -> aplicarFiltro("hortaliza"));
        view.findViewById(R.id.btnFruta).setOnClickListener(v    -> aplicarFiltro("fruta"));
        view.findViewById(R.id.btnHierba).setOnClickListener(v   -> aplicarFiltro("hierba"));
        view.findViewById(R.id.btnFlor).setOnClickListener(v     -> aplicarFiltro("flor"));
        view.findViewById(R.id.btnArbol).setOnClickListener(v    -> aplicarFiltro("arbol"));
    }

    // El filtrado se delega al DAO, el fragment solo actualiza la UI
    private void aplicarFiltro(String tipo) {
        filtradas.clear();
        filtradas.addAll(plantaDAO.filtrarPorTipo(plantas, tipo));
        adapter.updateList(filtradas);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Limpiar listener para evitar memory leaks
        if (plantaListener != null) {
            plantaDAO.removeListener(plantaListener);
        }
    }
}