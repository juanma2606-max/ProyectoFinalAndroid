package com.example.proyectofinal.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyectofinal.R;
import com.example.proyectofinal.adaptadores.CultivoAdapter;
import com.example.proyectofinal.dao.CultivoDAO;
import com.example.proyectofinal.dao.HuertoDAO;
import com.example.proyectofinal.modelos.Cultivo;
import com.example.proyectofinal.modelos.Huerto;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HuertoDetalleActivity extends AppCompatActivity {

    private TextView txtNombreHuerto, txtDescripcionHuerto;
    private ImageView imgHuerto;
    private RecyclerView recyclerCultivos;
    private LinearLayout emptyStateCultivos;
    private CultivoAdapter cultivoAdapter;
    private final List<Cultivo> cultivos = new ArrayList<>();

    private HuertoDAO huertoDAO;
    private CultivoDAO cultivoDAO;
    private ValueEventListener cultivoListener;

    private String huertoId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_huerto_detalle);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        huertoDAO  = new HuertoDAO();
        cultivoDAO = new CultivoDAO();

        txtNombreHuerto      = findViewById(R.id.txtNombreHuerto);
        txtDescripcionHuerto = findViewById(R.id.txtDescripcionHuerto);
        imgHuerto            = findViewById(R.id.imgHuertoDetalle);
        recyclerCultivos     = findViewById(R.id.recyclerCultivos);
        emptyStateCultivos   = findViewById(R.id.emptyStateCultivos);

        recyclerCultivos.setLayoutManager(new LinearLayoutManager(this));
        cultivoAdapter = new CultivoAdapter(this, cultivos, new CultivoAdapter.OnCultivoActionListener() {
            @Override
            public void onVer(Cultivo cultivo) {
                Intent i = new Intent(HuertoDetalleActivity.this, PlantaDetalleActivity.class);
                i.putExtra("id", cultivo.plantaId);
                startActivity(i);
            }

            @Override
            public void onEliminar(Cultivo cultivo) {
                eliminarCultivo(cultivo);
            }
        });
        recyclerCultivos.setAdapter(cultivoAdapter);

        // Botón volver
        ImageButton btnVolver = findViewById(R.id.btnVolver);
        btnVolver.setOnClickListener(v -> finish());

        // FAB agregar cultivo
        FloatingActionButton fab = findViewById(R.id.fabAgregarCultivo);
        fab.setOnClickListener(v -> {
            Intent i = new Intent(this, AgregarCultivoActivity.class);
            i.putExtra("huertoId", huertoId);
            startActivity(i);
        });

        // Recibir el ID del huerto
        huertoId = getIntent().getStringExtra("huertoId");
        cargarHuerto();
        cargarCultivos();
    }

    // ---------------------------------------------------------
    // Carga los datos del huerto — equivalente a getHuertoById()
    // ---------------------------------------------------------
    private void cargarHuerto() {
        huertoDAO.getHuertoById(huertoId, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Huerto huerto = snapshot.getValue(Huerto.class);
                if (huerto == null) return;

                txtNombreHuerto.setText(huerto.nombre);
                txtDescripcionHuerto.setText(
                        huerto.descripcion != null && !huerto.descripcion.isEmpty()
                                ? huerto.descripcion
                                : "Sin descripción");

                // Imagen según tipo
                boolean esParcela = "parcela".equals(huerto.tipo);
                imgHuerto.setImageResource(esParcela
                        ? R.drawable.img_parcela
                        : R.drawable.img_maceta);

                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(huerto.nombre);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HuertoDetalleActivity.this,
                        "Error al cargar el huerto", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ---------------------------------------------------------
    // Carga los cultivos del huerto en tiempo real
    // ---------------------------------------------------------
    private void cargarCultivos() {
        cultivoListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                cultivos.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Cultivo c = child.getValue(Cultivo.class);
                    if (c != null) {
                        c.id = child.getKey();
                        cultivos.add(c);
                    }
                }
                cultivoAdapter.updateList(cultivos);

                if (cultivos.isEmpty()) {
                    emptyStateCultivos.setVisibility(View.VISIBLE);
                    recyclerCultivos.setVisibility(View.GONE);
                } else {
                    emptyStateCultivos.setVisibility(View.GONE);
                    recyclerCultivos.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HuertoDetalleActivity.this,
                        "Error al cargar cultivos", Toast.LENGTH_SHORT).show();
            }
        };

        cultivoDAO.getCultivosByHuerto(huertoId, cultivoListener);
    }

    // ---------------------------------------------------------
    // Eliminar cultivo
    // ---------------------------------------------------------
    private void eliminarCultivo(Cultivo cultivo) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("¿Eliminar cultivo?")
                .setMessage("¿Seguro que quieres eliminar este cultivo?\nEsta acción no se puede deshacer.")
                .setPositiveButton("Eliminar", (d, w) ->
                        cultivoDAO.removeCultivo(huertoId, cultivo.id, new CultivoDAO.OnCompleteCallback() {
                            @Override
                            public void onSuccess() {
                                Toast.makeText(HuertoDetalleActivity.this,
                                        "Cultivo eliminado", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onError(String message) {
                                Toast.makeText(HuertoDetalleActivity.this,
                                        "Error: " + message, Toast.LENGTH_SHORT).show();
                            }
                        }))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cultivoListener != null) {
            cultivoDAO.removeListener(huertoId, cultivoListener);
        }
    }
}