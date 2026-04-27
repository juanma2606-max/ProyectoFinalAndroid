package com.example.proyectofinal.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyectofinal.R;
import com.example.proyectofinal.activities.AgregarHuertoActivity;
import com.example.proyectofinal.activities.HuertoDetalleActivity;
import com.example.proyectofinal.adaptadores.HuertoAdapter;
import com.example.proyectofinal.dao.HuertoDAO;
import com.example.proyectofinal.modelos.Huerto;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recycler;
    private HuertoAdapter adapter;
    private final List<Huerto> huertos = new ArrayList<>();
    private HuertoDAO huertoDAO;
    private ValueEventListener huertoListener;

    private ProgressBar progressBar;
    private LinearLayout emptyState;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        huertoDAO   = new HuertoDAO();
        progressBar = view.findViewById(R.id.progressBar);
        emptyState  = view.findViewById(R.id.emptyState);
        recycler    = view.findViewById(R.id.recyclerHuertos);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));

        FloatingActionButton fab = view.findViewById(R.id.fabAgregarHuerto);
        fab.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), AgregarHuertoActivity.class))
        );

        Button btnCrearPrimerHuerto = view.findViewById(R.id.btnCrearPrimerHuerto);
        btnCrearPrimerHuerto.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), AgregarHuertoActivity.class))
        );

        adapter = new HuertoAdapter(requireContext(), huertos, new HuertoAdapter.OnHuertoActionListener() {
            @Override
            public void onVer(Huerto huerto) {
                Intent i = new Intent(requireContext(), HuertoDetalleActivity.class);
                i.putExtra("huertoId", huerto.getId());
                startActivity(i);
            }

            @Override
            public void onEditar(Huerto huerto) {
                Intent i = new Intent(requireContext(), com.example.proyectofinal.activities.EditarHuertoActivity.class);
                i.putExtra("huertoId", huerto.getId());
                startActivity(i);
            }

            @Override
            public void onEliminar(Huerto huerto) {
                mostrarDialogoEliminar(huerto);
            }
        });

        recycler.setAdapter(adapter);

        cargarHuertos();
    }

    // ---------------------------------------------------------
    // Carga en tiempo real — equivalente a huertos$ | async
    // ---------------------------------------------------------
    private void cargarHuertos() {
        progressBar.setVisibility(View.VISIBLE);
        emptyState.setVisibility(View.GONE);
        recycler.setVisibility(View.GONE);

        huertoListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                huertos.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Huerto h = child.getValue(Huerto.class);
                    if (h != null) {
                        h.setId(child.getKey());  // ← CORREGIDO: usar setter en lugar de acceso directo
                        huertos.add(h);
                    }
                }
                adapter.updateList(huertos);
                progressBar.setVisibility(View.GONE);

                if (huertos.isEmpty()) {
                    emptyState.setVisibility(View.VISIBLE);
                    recycler.setVisibility(View.GONE);
                } else {
                    emptyState.setVisibility(View.GONE);
                    recycler.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Error al cargar huertos", Toast.LENGTH_SHORT).show();
            }
        };

        huertoDAO.getAllHuertos(huertoListener);
    }

    // ---------------------------------------------------------
    // Ver detalle — equivalente a routerLink parcela/maceta
    // ---------------------------------------------------------
    private void abrirDetalle(Huerto huerto) {
        // TODO: navegar a HuertoDetalleActivity o MacetaDetalleActivity según tipo
        Toast.makeText(requireContext(), "Ver: " + huerto.getNombre(), Toast.LENGTH_SHORT).show();
    }

    // ---------------------------------------------------------
    // Diálogo ELIMINAR — equivalente al modal de confirmación
    // ---------------------------------------------------------
    private void mostrarDialogoEliminar(Huerto huerto) {
        new AlertDialog.Builder(requireContext())
                .setTitle("¿Eliminar huerto?")
                .setMessage("¿Estás seguro de querer eliminar \"" + huerto.getNombre()
                        + "\"?\nEsta acción no se puede deshacer.")
                .setPositiveButton("Eliminar", (d, w) ->
                        // Delegamos al DAO — equivalente a huertoService.removeObject()
                        huertoDAO.removeHuerto(huerto.getId(), new HuertoDAO.OnCompleteCallback() {
                            @Override
                            public void onSuccess() {
                                Toast.makeText(requireContext(), "Huerto eliminado", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onError(String message) {
                                Toast.makeText(requireContext(), "Error: " + message, Toast.LENGTH_SHORT).show();
                            }
                        }))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // ---------------------------------------------------------
    // Limpiar listener al destruir la vista — equivalente a ngOnDestroy
    // ---------------------------------------------------------
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (huertoListener != null) {
            huertoDAO.removeListener(huertoListener);
        }
    }
}