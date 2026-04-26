package com.example.proyectofinal.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
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
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class HuertoDetalleActivity extends AppCompatActivity {

    private TextView txtNombreHuerto, txtDescripcionHuerto;
    private TextView txtUbicacion, txtSuperficie, txtTipoSuelo;
    private TextView txtHorasSol, txtRiego, txtNotas;
    private ImageView imgHuerto;
    private LinearLayout layoutInfoExtra, emptyStateCultivos;
    private RecyclerView recyclerCultivos;

    private CultivoAdapter cultivoAdapter;
    private final List<Cultivo> cultivos = new ArrayList<>();

    private HuertoDAO huertoDAO;
    private CultivoDAO cultivoDAO;
    private ValueEventListener cultivoListener;

    private String huertoId;
    private Huerto huertoActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_huerto_detalle);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        huertoDAO = new HuertoDAO();
        cultivoDAO = new CultivoDAO();

        // Referencias vistas básicas
        txtNombreHuerto = findViewById(R.id.txtNombreHuerto);
        txtDescripcionHuerto = findViewById(R.id.txtDescripcionHuerto);
        imgHuerto = findViewById(R.id.imgHuertoDetalle);

        // Referencias vistas nuevas (si existen en layout)
        txtUbicacion = findViewById(R.id.txtUbicacion);
        txtSuperficie = findViewById(R.id.txtSuperficie);
        txtTipoSuelo = findViewById(R.id.txtTipoSuelo);
        txtHorasSol = findViewById(R.id.txtHorasSol);
        txtRiego = findViewById(R.id.txtRiego);
        txtNotas = findViewById(R.id.txtNotas);
        layoutInfoExtra = findViewById(R.id.layoutInfoExtra);

        // RecyclerView cultivos
        recyclerCultivos = findViewById(R.id.recyclerCultivos);
        emptyStateCultivos = findViewById(R.id.emptyStateCultivos);

        recyclerCultivos.setLayoutManager(new LinearLayoutManager(this));
        cultivoAdapter = new CultivoAdapter(this, cultivos, new CultivoAdapter.OnCultivoActionListener() {
            @Override
            public void onVer(Cultivo cultivo) {
                Intent i = new Intent(HuertoDetalleActivity.this, PlantaDetalleActivity.class);
                i.putExtra("id", cultivo.getPlantaId());
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
        if (btnVolver != null) {
            btnVolver.setOnClickListener(v -> finish());
        }

        // Botón editar (si existe en layout)
        ImageButton btnEditar = findViewById(R.id.btnEditarHuerto);
        if (btnEditar != null) {
            btnEditar.setOnClickListener(v -> {
                Intent i = new Intent(this, EditarHuertoActivity.class);
                i.putExtra("huertoId", huertoId);
                startActivity(i);
            });
        }

        // FAB agregar cultivo
        FloatingActionButton fab = findViewById(R.id.fabAgregarCultivo);
        if (fab != null) {
            fab.setOnClickListener(v -> {
                Intent i = new Intent(this, AgregarCultivoActivity.class);
                i.putExtra("huertoId", huertoId);
                startActivity(i);
            });
        }

        // Recibir el ID del huerto
        huertoId = getIntent().getStringExtra("huertoId");
        if (huertoId == null || huertoId.isEmpty()) {
            Toast.makeText(this, "Error: ID de huerto no válido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        cargarHuerto();
        cargarCultivos();
    }

    // ---------------------------------------------------------
    // Carga los datos del huerto con nuevo modelo
    // ---------------------------------------------------------
    private void cargarHuerto() {
        huertoDAO.getHuertoById(huertoId, new HuertoDAO.OnHuertoLoadedCallback() {
            @Override
            public void onLoaded(Huerto huerto) {
                huertoActual = huerto;
                mostrarDatosHuerto(huerto);
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(HuertoDetalleActivity.this,
                        "Error al cargar huerto: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    // ---------------------------------------------------------
    // Mostrar datos del huerto en UI
    // ---------------------------------------------------------
    private void mostrarDatosHuerto(Huerto huerto) {
        // Datos básicos
        txtNombreHuerto.setText(huerto.getNombre());

        String descripcion = huerto.getDescripcion();
        if (descripcion != null && !descripcion.isEmpty()) {
            txtDescripcionHuerto.setText(descripcion);
            txtDescripcionHuerto.setVisibility(View.VISIBLE);
        } else {
            txtDescripcionHuerto.setVisibility(View.GONE);
        }

        // Imagen
        if (huerto.tieneFoto()) {
            Picasso.get()
                    .load(huerto.getFoto())
                    .placeholder(R.drawable.img_huerto_default)
                    .error(R.drawable.img_huerto_default)
                    .into(imgHuerto);
        } else {
            imgHuerto.setImageResource(R.drawable.img_huerto_default);
        }

        // Datos adicionales (si los TextViews existen en layout)
        if (txtUbicacion != null) {
            String ubicacion = huerto.getUbicacion();
            if (ubicacion != null && !ubicacion.isEmpty()) {
                txtUbicacion.setText("📍 " + ubicacion);
                txtUbicacion.setVisibility(View.VISIBLE);
            } else {
                txtUbicacion.setVisibility(View.GONE);
            }
        }

        if (txtSuperficie != null) {
            txtSuperficie.setText("📏 " + huerto.getSuperficieTexto());
        }

        if (txtTipoSuelo != null) {
            txtTipoSuelo.setText("🌱 Suelo: " + huerto.getTipoSueloCapitalizado());
        }

        if (txtHorasSol != null) {
            txtHorasSol.setText("☀️ " + huerto.getHorasSolTexto());
        }

        if (txtRiego != null) {
            txtRiego.setText("💧 Riego: " + huerto.getRiegoTexto());
        }

        if (txtNotas != null) {
            if (huerto.tieneNotas()) {
                txtNotas.setText(huerto.getNotas());
                txtNotas.setVisibility(View.VISIBLE);
            } else {
                txtNotas.setVisibility(View.GONE);
            }
        }

        // ActionBar título
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(huerto.getNombre());
        }
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
                        c.setId(child.getKey());
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
                        "Error al cargar cultivos: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
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
                        cultivoDAO.removeCultivo(huertoId, cultivo.getId(), new CultivoDAO.OnCompleteCallback() {
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

    @Override
    protected void onResume() {
        super.onResume();
        // Recargar huerto por si fue editado
        if (huertoId != null) {
            cargarHuerto();
        }
    }
}