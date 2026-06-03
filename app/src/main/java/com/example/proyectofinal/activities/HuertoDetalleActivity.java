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
import com.example.proyectofinal.dialogs.DialogAnalisisHuerto;
import com.example.proyectofinal.modelos.Cultivo;
import com.example.proyectofinal.modelos.Huerto;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HuertoDetalleActivity extends AppCompatActivity {

    private TextView txtNombreHuerto, txtDescripcionHuerto;
    private TextView txtUbicacion, txtSuperficie, txtTipoSuelo;
    private TextView txtHorasSol, txtRiego, txtNotas;
    private ImageView imgHuerto;
    private MaterialCardView cardInfoHuerto, cardNotas;
    private LinearLayout emptyStateCultivos;
    private RecyclerView recyclerCultivos;

    // Cards colapsables
    private LinearLayout headerDescripcion, headerInfo, headerNotas;
    private LinearLayout contentDescripcion, contentInfo, contentNotas;
    private ImageView iconDescripcion, iconInfo, iconNotas;

    private CultivoAdapter cultivoAdapter;
    private final List<Cultivo> cultivos = new ArrayList<>();

    private HuertoDAO huertoDAO;
    private CultivoDAO cultivoDAO;
    private ValueEventListener cultivoListener;

    private String huertoId;
    private Huerto huertoActual;

    private Button btnAnalizarHuerto;

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

        // Referencias vistas info huerto
        txtUbicacion = findViewById(R.id.txtUbicacion);
        txtSuperficie = findViewById(R.id.txtSuperficie);
        txtTipoSuelo = findViewById(R.id.txtTipoSuelo);
        txtHorasSol = findViewById(R.id.txtHorasSol);
        txtRiego = findViewById(R.id.txtRiego);
        txtNotas = findViewById(R.id.txtNotas);
        cardInfoHuerto = findViewById(R.id.cardInfoHuerto);
        cardNotas = findViewById(R.id.cardNotas);

        // Referencias cards colapsables
        headerDescripcion = findViewById(R.id.headerDescripcion);
        headerInfo = findViewById(R.id.headerInfo);
        headerNotas = findViewById(R.id.headerNotas);

        contentDescripcion = findViewById(R.id.contentDescripcion);
        contentInfo = findViewById(R.id.contentInfo);
        contentNotas = findViewById(R.id.contentNotas);

        iconDescripcion = findViewById(R.id.iconDescripcion);
        iconInfo = findViewById(R.id.iconInfo);
        iconNotas = findViewById(R.id.iconNotas);

        // RecyclerView cultivos
        recyclerCultivos = findViewById(R.id.recyclerCultivos);
        emptyStateCultivos = findViewById(R.id.emptyStateCultivos);

        // Botón de ia
        btnAnalizarHuerto = findViewById(R.id.btnAnalizarHuerto);

        recyclerCultivos.setLayoutManager(new LinearLayoutManager(this));
        recyclerCultivos.setNestedScrollingEnabled(false);
        recyclerCultivos.setHasFixedSize(false);
        cultivoAdapter = new CultivoAdapter(this, cultivos, new CultivoAdapter.OnCultivoActionListener() {
            @Override
            public void onEditar(Cultivo cultivo) {
                Intent i = new Intent(HuertoDetalleActivity.this, com.example.proyectofinal.activities.EditarCultivoActivity.class);
                i.putExtra("huertoId", huertoId);
                i.putExtra("cultivoId", cultivo.getId());
                startActivity(i);
            }

            @Override
            public void onEliminar(Cultivo cultivo) {
                eliminarCultivo(cultivo);
            }
        });
        recyclerCultivos.setAdapter(cultivoAdapter);

        // Setup listeners colapsar/expandir
        setupCollapsible(headerDescripcion, contentDescripcion, iconDescripcion);
        setupCollapsible(headerInfo, contentInfo, iconInfo);
        setupCollapsible(headerNotas, contentNotas, iconNotas);

        // Botón volver
        ImageButton btnVolver = findViewById(R.id.btnVolver);
        if (btnVolver != null) {
            btnVolver.setOnClickListener(v -> finish());
        }

        // Botón editar
        ImageButton btnEditar = findViewById(R.id.btnEditarHuerto);
        if (btnEditar != null) {
            btnEditar.setOnClickListener(v -> {
                Intent i = new Intent(HuertoDetalleActivity.this, EditarHuertoActivity.class);
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

        btnAnalizarHuerto.setOnClickListener(v -> analizarHuertoConIA());

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
    // Configurar card colapsable
    // ---------------------------------------------------------
    private void setupCollapsible(LinearLayout header, LinearLayout content, ImageView icon) {
        if (header == null || content == null || icon == null) return;

        header.setOnClickListener(v -> {
            boolean isVisible = content.getVisibility() == View.VISIBLE;

            if (isVisible) {
                // Colapsar
                content.setVisibility(View.GONE);
                icon.setRotation(0); // Flecha hacia abajo
            } else {
                // Expandir
                content.setVisibility(View.VISIBLE);
                icon.setRotation(180); // Flecha hacia arriba
            }
        });
    }

    // ---------------------------------------------------------
    // Carga los datos del huerto
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
        // Nombre (en toolbar)
        txtNombreHuerto.setText(huerto.getNombre());

        // Descripción
        if (huerto.getDescripcion() != null && !huerto.getDescripcion().isEmpty()) {
            txtDescripcionHuerto.setText(huerto.getDescripcion());
        } else {
            txtDescripcionHuerto.setText("Sin descripción");
        }

        // IMAGEN - Cargar desde drawable según nombre seleccionado
        if (huerto.tieneFoto()) {
            String foto = huerto.getFoto(); // "huerto1.jpg", "huerto2.webp", etc.

            // Quitar extensión para buscar drawable
            String nombreSinExtension = foto
                    .replace(".jpg", "")
                    .replace(".webp", "")
                    .replace(".png", "");

            // Buscar drawable por nombre
            int resId = getResources().getIdentifier(
                    nombreSinExtension,  // "huerto1", "huerto2", "huerto3"
                    "drawable",
                    getPackageName()
            );

            if (resId != 0) {
                // Imagen encontrada - cargarla
                imgHuerto.setImageResource(resId);
            } else {
                // Imagen no encontrada - placeholder genérico
                imgHuerto.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        } else {
            // Sin foto - placeholder genérico
            imgHuerto.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        // Info huerto (con null checks)
        if (txtUbicacion != null) {
            txtUbicacion.setText("📍 " + huerto.getUbicacion());
        }

        if (txtSuperficie != null) {
            txtSuperficie.setText("📏 " + huerto.getSuperficieTexto());
        }

        if (txtTipoSuelo != null) {
            txtTipoSuelo.setText("🌱 " + huerto.getTipoSueloCapitalizado());
        }

        if (txtHorasSol != null) {
            txtHorasSol.setText("☀️ " + huerto.getHorasSolTexto());
        }

        if (txtRiego != null) {
            txtRiego.setText("💧 " + huerto.getRiegoTexto());
        }

        // Notas (solo si hay)
        if (cardNotas != null && txtNotas != null) {
            if (huerto.tieneNotas()) {
                cardNotas.setVisibility(View.VISIBLE);
                txtNotas.setText(huerto.getNotas());
            } else {
                cardNotas.setVisibility(View.GONE);
            }
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
                List<String> ids = new ArrayList<>();
                for (Cultivo c : cultivos) {
                    ids.add(c.getPlantaId());
                }
                cultivoAdapter.setPlantasEnHuerto(ids);
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

    private void analizarHuertoConIA() {
        StringBuilder prompt = new StringBuilder();

        // INSTRUCCIONES CLARAS PRIMERO
        prompt.append("INSTRUCCIONES: Analiza mi huerto respondiendo en este formato:\n");
        prompt.append("1. ANÁLISIS INDIVIDUAL de cada cultivo (uno por uno)\n");
        prompt.append("2. ANÁLISIS GENERAL del huerto completo\n\n");
        prompt.append("═══════════════════════════════════\n\n");

        // DATOS HUERTO
        prompt.append("📋 MI HUERTO:\n");
        prompt.append("🌱 Nombre: ").append(huertoActual.getNombre()).append("\n");
        prompt.append("📍 Ubicación: ").append(huertoActual.getUbicacion()).append("\n");
        prompt.append("📏 Superficie: ").append(huertoActual.getSuperficie()).append(" m²\n");
        prompt.append("🌾 Tipo suelo: ").append(huertoActual.getTipo_suelo()).append("\n");
        prompt.append("☀️ Horas sol: ").append(huertoActual.getHoras_sol()).append(" h/día\n");
        prompt.append("💧 Riego: ").append(huertoActual.getTiene_riego() ? "Sí" : "No").append("\n");

        if (huertoActual.getNotas() != null && !huertoActual.getNotas().isEmpty()) {
            prompt.append("📝 Notas: ").append(huertoActual.getNotas()).append("\n");
        }

        // CULTIVOS CON NOMBRE
        if (!cultivos.isEmpty()) {
            prompt.append("\n🌿 CULTIVOS (").append(cultivos.size()).append("):\n\n");

            for (int i = 0; i < cultivos.size(); i++) {
                Cultivo c = cultivos.get(i);

                prompt.append("Cultivo ").append(i + 1).append(":\n");
                prompt.append("  • Nombre: ").append(c.getNombre() != null ? c.getNombre() : "Sin nombre").append("\n");
                prompt.append("  • Cantidad: ").append(c.getCantidad()).append(" planta");
                if (c.getCantidad() > 1) prompt.append("s");
                prompt.append("\n");
                prompt.append("  • Estado: ").append(c.getEstado());

                if (c.estaEnfermo()) {
                    prompt.append(" ⚠️ ENFERMO");
                    if (c.getAmenazaId() != null) {
                        prompt.append(" (amenaza ").append(c.getAmenazaId()).append(")");
                    }
                }
                prompt.append("\n");

                if (c.getFechaSiembra() != null) {
                    prompt.append("  • Fecha siembra: ").append(c.getFechaSiembra()).append("\n");
                }

                if (c.getNotas() != null && !c.getNotas().isEmpty()) {
                    prompt.append("  • Notas: ").append(c.getNotas()).append("\n");
                }

                prompt.append("\n");
            }
        }

        // QUÉ QUIERO EN LA RESPUESTA
        prompt.append("═══════════════════════════════════\n");
        prompt.append("📝 RESPONDE ASÍ:\n");
        prompt.append("═══════════════════════════════════\n\n");

        if (!cultivos.isEmpty()) {
            prompt.append("🔍 PARTE 1 - ANÁLISIS INDIVIDUAL:\n");
            prompt.append("Analiza CADA cultivo separadamente:\n\n");

            for (Cultivo c : cultivos) {
                String nombre = c.getNombre() != null ? c.getNombre() : "Sin nombre";
                prompt.append("**").append(nombre).append(":**\n");

                if (c.estaEnfermo()) {
                    prompt.append("- Diagnóstico: ¿qué tiene?\n");
                    prompt.append("- Causas probables\n");
                    prompt.append("- Tratamiento recomendado\n");
                    prompt.append("- ¿Salvar o arrancar?\n");
                } else {
                    prompt.append("- Estado actual\n");
                    prompt.append("- Cuidados necesarios ahora\n");
                    prompt.append("- Cuándo cosechar\n");
                    prompt.append("- Consejos extra\n");
                }
                prompt.append("\n");
            }

            prompt.append("🌍 PARTE 2 - ANÁLISIS GENERAL:\n");
            prompt.append("- Compatibilidad entre cultivos\n");
            prompt.append("- Nuevos cultivos recomendados\n");
            prompt.append("- Optimización espacio\n");
            prompt.append("- Calendario siembra próximas semanas\n");

        } else {
            prompt.append("Como NO tengo cultivos, dame:\n");
            prompt.append("- Qué plantar según suelo y sol\n");
            prompt.append("- Calendario siembra inicial\n");
        }

        // Mostrar dialog
        DialogAnalisisHuerto dialog = new DialogAnalisisHuerto(this, prompt.toString());
        dialog.show();
    }
}