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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AgregarCultivoActivity extends AppCompatActivity {

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
    private String adminUid;
    private Calendar calendarioSeleccionado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_cultivo);

        plantaDAO = new PlantaDAO();
        cultivoDAO = new CultivoDAO();
        amenazaDAO = new AmenazaDAO();
        calendarioSeleccionado = Calendar.getInstance();

        huertoId = getIntent().getStringExtra("huertoId");
        adminUid = getIntent().getStringExtra("adminUid");

        if (esModoAdmin() && getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Añadir cultivo (admin)");
        }

        // Inicializar vistas
        rvPlantas = findViewById(R.id.rvPlantas);
        etNombre = findViewById(R.id.etNombreCultivo);
        etCantidad = findViewById(R.id.etCantidad);
        etFechaSiembra = findViewById(R.id.etFechaSiembra);
        etNotas = findViewById(R.id.etNotas);
        rgEstado = findViewById(R.id.rgEstado);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnCancelar = findViewById(R.id.btnCancelar);

        // Selector amenazas
        cardAmenazas = findViewById(R.id.cardAmenazas);
        spinnerAmenazas = findViewById(R.id.spinnerAmenazas);

        // Configurar RecyclerView
        rvPlantas.setLayoutManager(new GridLayoutManager(this, 3));

        // Configurar fecha por defecto (hoy)
        actualizarFechaUI();

        // Configurar listeners
        etFechaSiembra.setOnClickListener(v -> mostrarDatePicker());
        btnCancelar.setOnClickListener(v -> finish());
        btnGuardar.setOnClickListener(v -> guardarCultivo());

        // Listener cambio estado
        rgEstado.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbEnfermo) {
                cardAmenazas.setVisibility(View.VISIBLE);
            } else {
                cardAmenazas.setVisibility(View.GONE);
            }
        });

        cargarPlantas();
        cargarAmenazas();
    }

    // ---------------------------------------------------------
    // ¿Estamos creando el cultivo en el huerto de otro usuario?
    // ---------------------------------------------------------
    private boolean esModoAdmin() {
        return adminUid != null && !adminUid.isEmpty();
    }

    // ---------------------------------------------------------
    // Carga amenazas desde Firebase
    // ---------------------------------------------------------
    private void cargarAmenazas() {
        amenazaDAO.getAllAmenazasOnce(new AmenazaDAO.OnAmenazasLoadedCallback() {
            @Override
            public void onLoaded(List<Amenaza> amenazas) {
                listaAmenazas.clear();
                listaAmenazas.addAll(amenazas);

                // Crear lista de nombres para Spinner
                List<String> nombresAmenazas = new ArrayList<>();
                nombresAmenazas.add("Seleccionar amenaza...");
                for (Amenaza a : amenazas) {
                    nombresAmenazas.add(a.getNombre());
                }

                // Configurar Spinner
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        AgregarCultivoActivity.this,
                        android.R.layout.simple_spinner_item,
                        nombresAmenazas
                );
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerAmenazas.setAdapter(adapter);
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(AgregarCultivoActivity.this,
                        "Error al cargar amenazas", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ---------------------------------------------------------
    // Carga el catálogo de plantas en RecyclerView
    // ---------------------------------------------------------
    private void cargarPlantas() {
        plantaDAO.getAllPlantasOnce(new PlantaDAO.OnPlantasLoadedCallback() {
            @Override
            public void onLoaded(List<Planta> plantas) {
                listaPlantas.clear();
                listaPlantas.addAll(plantas);

                plantaAdapter = new PlantaSelectorAdapter(
                        AgregarCultivoActivity.this,
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
                Toast.makeText(AgregarCultivoActivity.this,
                        "Error al cargar plantas", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ---------------------------------------------------------
    // Mostrar DatePicker para seleccionar fecha
    // ---------------------------------------------------------
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

    // ---------------------------------------------------------
    // Actualizar campo de fecha en UI
    // ---------------------------------------------------------
    private void actualizarFechaUI() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        etFechaSiembra.setText(sdf.format(calendarioSeleccionado.getTime()));
    }

    // ---------------------------------------------------------
    // Convertir fecha a formato ISO
    // ---------------------------------------------------------
    private String getFechaISO() {
        SimpleDateFormat sdfISO = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        return sdfISO.format(calendarioSeleccionado.getTime());
    }

    // ---------------------------------------------------------
    // Obtener amenazaId seleccionado
    // ---------------------------------------------------------
    private String getAmenazaIdSeleccionada() {
        int posicion = spinnerAmenazas.getSelectedItemPosition();

        // Posición 0 = "Seleccionar amenaza..." (placeholder)
        if (posicion <= 0 || posicion > listaAmenazas.size()) {
            return null;
        }

        // Restar 1 porque el placeholder está en posición 0
        Amenaza amenaza = listaAmenazas.get(posicion - 1);
        return amenaza.getId();
    }

    // ---------------------------------------------------------
    // Guarda el cultivo en Firebase
    // ---------------------------------------------------------
    private void guardarCultivo() {
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
        String fechaISO = getFechaISO();

        // Obtener amenazaId si enfermo
        String amenazaId = null;
        if (estado.equals("enfermo")) {
            amenazaId = getAmenazaIdSeleccionada();

            if (amenazaId == null) {
                Toast.makeText(this, "Selecciona una amenaza para cultivo enfermo", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Crear objeto Cultivo
        Cultivo nuevo = new Cultivo(
                null,                       // id (Firebase lo genera)
                nombre,                     // nombre
                plantaSeleccionada.getId(), // plantaId
                fechaISO,                   // fecha_siembra
                cantidad,                   // cantidad
                estado,                     // estado
                amenazaId,                  // amenazaId
                notas                       // notas
        );

        btnGuardar.setEnabled(false);

        CultivoDAO.OnCompleteCallback callback = new CultivoDAO.OnCompleteCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(AgregarCultivoActivity.this,
                        "Cultivo añadido ✅", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String message) {
                btnGuardar.setEnabled(true);
                Toast.makeText(AgregarCultivoActivity.this,
                        "Error: " + message, Toast.LENGTH_SHORT).show();
            }
        };

        if (esModoAdmin()) {
            cultivoDAO.createCultivoForUser(adminUid, huertoId, nuevo, callback);
        } else {
            cultivoDAO.createCultivo(huertoId, nuevo, callback);
        }
    }

    // ---------------------------------------------------------
    // Lee el estado seleccionado en el RadioGroup
    // ---------------------------------------------------------
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
            return "plantado"; // Por defecto
        }
    }
}