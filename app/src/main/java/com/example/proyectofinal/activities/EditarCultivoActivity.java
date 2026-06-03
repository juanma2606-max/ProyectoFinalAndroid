package com.example.proyectofinal.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyectofinal.R;
import com.example.proyectofinal.adaptadores.PlantaSelectorAdapter;
import com.example.proyectofinal.dao.AmenazaDAO;
import com.example.proyectofinal.dao.CultivoDAO;
import com.example.proyectofinal.dao.PlantaDAO;
import com.example.proyectofinal.modelos.Amenaza;
import com.example.proyectofinal.modelos.Cultivo;
import com.example.proyectofinal.modelos.Planta;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class EditarCultivoActivity extends AppCompatActivity {

    private RecyclerView rvPlantas;
    private TextInputEditText etNombre;
    private TextInputEditText etCantidad;
    private TextInputEditText etFechaSiembra;
    private TextInputEditText etNotas;
    private RadioGroup rgEstado;
    private Button btnGuardar;
    private Button btnCancelar;

    // Selector amenazas
    private MaterialCardView cardAmenazas;
    private Spinner spinnerAmenazas;

    private PlantaDAO plantaDAO;
    private CultivoDAO cultivoDAO;
    private AmenazaDAO amenazaDAO;
    private PlantaSelectorAdapter plantaAdapter;

    private final List<Planta> listaPlantas = new ArrayList<>();
    private final List<Amenaza> listaAmenazas = new ArrayList<>();
    private Planta plantaSeleccionada = null;
    private String huertoId;
    private String cultivoId;
    private Cultivo cultivoActual;
    private Calendar calendarioSeleccionado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_cultivo);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Editar cultivo");
        }

        plantaDAO = new PlantaDAO();
        cultivoDAO = new CultivoDAO();
        amenazaDAO = new AmenazaDAO();
        calendarioSeleccionado = Calendar.getInstance();

        huertoId = getIntent().getStringExtra("huertoId");
        cultivoId = getIntent().getStringExtra("cultivoId");

        if (huertoId == null || cultivoId == null) {
            Toast.makeText(this, "Error: datos inválidos", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        cargarPlantas();
        cargarAmenazas();
        cargarCultivo();

        // Listeners
        etFechaSiembra.setOnClickListener(v -> mostrarDatePicker());
        btnCancelar.setOnClickListener(v -> finish());
        btnGuardar.setOnClickListener(v -> guardarCambios());

        // Listener cambio estado
        rgEstado.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbEnfermo) {
                cardAmenazas.setVisibility(View.VISIBLE);
            } else {
                cardAmenazas.setVisibility(View.GONE);
            }
        });
    }

    private void initViews() {
        rvPlantas = findViewById(R.id.rvPlantas);
        etNombre = findViewById(R.id.etNombreCultivo);
        etCantidad = findViewById(R.id.etCantidad);
        etFechaSiembra = findViewById(R.id.etFechaSiembra);
        etNotas = findViewById(R.id.etNotas);
        rgEstado = findViewById(R.id.rgEstado);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnCancelar = findViewById(R.id.btnCancelar);

        cardAmenazas = findViewById(R.id.cardAmenazas);
        spinnerAmenazas = findViewById(R.id.spinnerAmenazas);

        // Configurar RecyclerView
        rvPlantas.setLayoutManager(new GridLayoutManager(this, 3));

        // Cambiar texto botón
        btnGuardar.setText("Guardar cambios");
    }

    private void cargarAmenazas() {
        amenazaDAO.getAllAmenazasOnce(new AmenazaDAO.OnAmenazasLoadedCallback() {
            @Override
            public void onLoaded(List<Amenaza> amenazas) {
                listaAmenazas.clear();
                listaAmenazas.addAll(amenazas);

                List<String> nombresAmenazas = new ArrayList<>();
                nombresAmenazas.add("Seleccionar amenaza...");
                for (Amenaza a : amenazas) {
                    nombresAmenazas.add(a.getNombre());
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        EditarCultivoActivity.this,
                        android.R.layout.simple_spinner_item,
                        nombresAmenazas
                );
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerAmenazas.setAdapter(adapter);
                // Restaurar amenaza si el cultivo ya estaba cargado
                if (cultivoActual != null && cultivoActual.estaEnfermo()
                        && cultivoActual.getAmenazaId() != null) {
                    seleccionarAmenaza(cultivoActual.getAmenazaId());
                }
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(EditarCultivoActivity.this,
                        "Error al cargar amenazas", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarPlantas() {
        plantaDAO.getAllPlantasOnce(new PlantaDAO.OnPlantasLoadedCallback() {
            @Override
            public void onLoaded(List<Planta> plantas) {
                listaPlantas.clear();
                listaPlantas.addAll(plantas);

                plantaAdapter = new PlantaSelectorAdapter(
                        EditarCultivoActivity.this,
                        listaPlantas,
                        planta -> {
                            plantaSeleccionada = planta;
                            plantaAdapter.notifyDataSetChanged();
                        }
                );

                rvPlantas.setAdapter(plantaAdapter);
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(EditarCultivoActivity.this,
                        "Error al cargar plantas", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarCultivo() {
        cultivoDAO.getCultivoById(huertoId, cultivoId, new CultivoDAO.OnCultivoLoadedCallback() {
            @Override
            public void onLoaded(Cultivo cultivo) {
                cultivoActual = cultivo;
                mostrarDatos(cultivo);
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(EditarCultivoActivity.this,
                        "Error al cargar cultivo: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void mostrarDatos(Cultivo cultivo) {
        // Nombre
        etNombre.setText(cultivo.getNombre());

        // Cantidad
        etCantidad.setText(String.valueOf(cultivo.getCantidad()));

        // Notas
        etNotas.setText(cultivo.getNotas());

        // Fecha siembra
        if (cultivo.getFechaSiembra() != null) {
            parsearYMostrarFecha(cultivo.getFechaSiembra());
        }

        // Estado
        seleccionarEstado(cultivo.getEstado());

        // Amenaza (si enfermo)
        if (cultivo.estaEnfermo() && cultivo.getAmenazaId() != null) {
            cardAmenazas.setVisibility(View.VISIBLE);
            seleccionarAmenaza(cultivo.getAmenazaId());
        }

        // Planta (marcar seleccionada cuando carguen)
        if (listaPlantas.isEmpty()) {
            // Esperar a que carguen plantas
            plantaDAO.getAllPlantasOnce(new PlantaDAO.OnPlantasLoadedCallback() {
                @Override
                public void onLoaded(List<Planta> plantas) {
                    for (Planta p : plantas) {
                        if (p.getId().equals(cultivo.getPlantaId())) {
                            plantaSeleccionada = p;
                            if (plantaAdapter != null) {
                                plantaAdapter.notifyDataSetChanged();
                            }
                            break;
                        }
                    }
                }

                @Override
                public void onError(Exception e) {}
            });
        }
    }

    private void parsearYMostrarFecha(String fechaISO) {
        try {
            SimpleDateFormat sdfISO = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            calendarioSeleccionado.setTime(sdfISO.parse(fechaISO));
            actualizarFechaUI();
        } catch (ParseException e) {
            // Si falla, intentar solo la fecha
            try {
                if (fechaISO.length() >= 10) {
                    String fecha = fechaISO.substring(0, 10);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    calendarioSeleccionado.setTime(sdf.parse(fecha));
                    actualizarFechaUI();
                }
            } catch (ParseException ex) {
                etFechaSiembra.setText(fechaISO);
            }
        }
    }

    private void seleccionarEstado(String estado) {
        switch (estado.toLowerCase()) {
            case "plantado":
                rgEstado.check(R.id.rbPlantado);
                break;
            case "creciendo":
                rgEstado.check(R.id.rbCreciendo);
                break;
            case "maduro":
                rgEstado.check(R.id.rbMaduro);
                break;
            case "cosechado":
                rgEstado.check(R.id.rbCosechado);
                break;
            case "enfermo":
                rgEstado.check(R.id.rbEnfermo);
                cardAmenazas.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void seleccionarAmenaza(String amenazaId) {
        for (int i = 0; i < listaAmenazas.size(); i++) {
            if (listaAmenazas.get(i).getId().equals(amenazaId)) {
                spinnerAmenazas.setSelection(i + 1); // +1 por placeholder
                break;
            }
        }
    }

    private void mostrarDatePicker() {
        int año = calendarioSeleccionado.get(Calendar.YEAR);
        int mes = calendarioSeleccionado.get(Calendar.MONTH);
        int dia = calendarioSeleccionado.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendarioSeleccionado.set(year, month, dayOfMonth);
                    actualizarFechaUI();
                },
                año, mes, dia
        );

        datePickerDialog.show();
    }

    private void actualizarFechaUI() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        etFechaSiembra.setText(sdf.format(calendarioSeleccionado.getTime()));
    }

    private String getFechaISO() {
        SimpleDateFormat sdfISO = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        return sdfISO.format(calendarioSeleccionado.getTime());
    }

    private String getAmenazaIdSeleccionada() {
        int posicion = spinnerAmenazas.getSelectedItemPosition();

        if (posicion <= 0 || posicion > listaAmenazas.size()) {
            return null;
        }

        Amenaza amenaza = listaAmenazas.get(posicion - 1);
        return amenaza.getId();
    }

    private void guardarCambios() {
        // Validar planta seleccionada
        if (plantaSeleccionada == null) {
            Toast.makeText(this, "Selecciona una planta", Toast.LENGTH_SHORT).show();
            return;
        }

        // Obtener valores
        String nombre = etNombre.getText() != null
                ? etNombre.getText().toString().trim()
                : "Cultivo sin nombre";

        if (nombre.isEmpty()) {
            nombre = "Cultivo sin nombre";
        }

        String cantidadStr = etCantidad.getText() != null
                ? etCantidad.getText().toString().trim()
                : "1";

        int cantidad;
        try {
            cantidad = Integer.parseInt(cantidadStr);
            if (cantidad < 1) {
                Toast.makeText(this, "La cantidad debe ser al menos 1", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Cantidad inválida", Toast.LENGTH_SHORT).show();
            return;
        }

        String notas = etNotas.getText() != null
                ? etNotas.getText().toString().trim()
                : "";

        String estado = getEstadoSeleccionado();

        // Obtener amenazaId si enfermo
        String amenazaId = null;
        if (estado.equals("enfermo")) {
            amenazaId = getAmenazaIdSeleccionada();

            if (amenazaId == null) {
                Toast.makeText(this, "Selecciona una amenaza para cultivo enfermo", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Actualizar objeto
        cultivoActual.setNombre(nombre);
        cultivoActual.setPlantaId(plantaSeleccionada.getId());
        cultivoActual.setCantidad(cantidad);
        cultivoActual.setEstado(estado);
        cultivoActual.setNotas(notas);
        cultivoActual.setAmenazaId(amenazaId);

        btnGuardar.setEnabled(false);

        cultivoDAO.updateCultivo(huertoId, cultivoActual, new CultivoDAO.OnCompleteCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(EditarCultivoActivity.this,
                        "Cultivo actualizado ✅", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String message) {
                btnGuardar.setEnabled(true);
                Toast.makeText(EditarCultivoActivity.this,
                        "Error: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getEstadoSeleccionado() {
        int checkedId = rgEstado.getCheckedRadioButtonId();

        if (checkedId == R.id.rbPlantado) {
            return "plantado";
        } else if (checkedId == R.id.rbCreciendo) {
            return "creciendo";
        } else if (checkedId == R.id.rbMaduro) {
            return "maduro";
        } else if (checkedId == R.id.rbCosechado) {
            return "cosechado";
        } else if (checkedId == R.id.rbEnfermo) {
            return "enfermo";
        } else {
            return "plantado";
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}