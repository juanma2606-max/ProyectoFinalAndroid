package com.example.proyectofinal.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.proyectofinal.R;
import com.example.proyectofinal.dao.CultivoDAO;
import com.example.proyectofinal.dao.PlantaDAO;
import com.example.proyectofinal.modelos.Cultivo;
import com.example.proyectofinal.modelos.Planta;
import android.widget.RadioGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AgregarCultivoActivity extends AppCompatActivity {

    private Spinner spinnerPlantas;
    private TextInputEditText etNotas;
    private RadioGroup chipGroupEstado;
    private Button btnGuardar;

    private PlantaDAO plantaDAO;
    private CultivoDAO cultivoDAO;

    private final List<Planta> listaPlantas = new ArrayList<>();
    private Planta plantaSeleccionada = null;
    private String huertoId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_cultivo);

        plantaDAO = new PlantaDAO();
        cultivoDAO = new CultivoDAO();

        huertoId = getIntent().getStringExtra("huertoId");

        spinnerPlantas  = findViewById(R.id.spinnerPlantas);
        etNotas         = findViewById(R.id.etNotas);
        chipGroupEstado = findViewById(R.id.chipGroupEstado);
        btnGuardar      = findViewById(R.id.btnGuardar);
        Button btnCancelar = findViewById(R.id.btnCancelar);

        btnCancelar.setOnClickListener(v -> finish());
        btnGuardar.setOnClickListener(v -> guardarCultivo());

        cargarPlantas();
    }

    // ---------------------------------------------------------
    // Carga el catálogo de plantas en el Spinner
    // ---------------------------------------------------------
    private void cargarPlantas() {
        plantaDAO.getAllPlantas(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaPlantas.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Planta p = child.getValue(Planta.class);
                    if (p != null) {
                        p.id = child.getKey();
                        listaPlantas.add(p);
                    }
                }

                // Nombres para mostrar en el Spinner
                List<String> nombres = new ArrayList<>();
                nombres.add("Selecciona una planta...");
                for (Planta p : listaPlantas) nombres.add(p.nombre);

                ArrayAdapter<String> adapterSpinner = new ArrayAdapter<>(
                        AgregarCultivoActivity.this,
                        android.R.layout.simple_spinner_item,
                        nombres);
                adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerPlantas.setAdapter(adapterSpinner);

                spinnerPlantas.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        // posición 0 es el placeholder
                        plantaSeleccionada = position > 0 ? listaPlantas.get(position - 1) : null;
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        plantaSeleccionada = null;
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AgregarCultivoActivity.this,
                        "Error al cargar plantas", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ---------------------------------------------------------
    // Guarda el cultivo en Firebase
    // ---------------------------------------------------------
    private void guardarCultivo() {
        if (plantaSeleccionada == null) {
            Toast.makeText(this, "Selecciona una planta", Toast.LENGTH_SHORT).show();
            return;
        }

        String notas  = etNotas.getText() != null ? etNotas.getText().toString().trim() : "";
        String estado = getEstadoSeleccionado();

        Cultivo nuevo = new Cultivo(null, plantaSeleccionada.id, null, notas, estado);

        btnGuardar.setEnabled(false);

        cultivoDAO.createCultivo(huertoId, nuevo, new CultivoDAO.OnCompleteCallback() {
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
        });
    }

    // ---------------------------------------------------------
    // Lee el estado seleccionado en el ChipGroup
    // ---------------------------------------------------------
    private String getEstadoSeleccionado() {
        int checkedId = chipGroupEstado.getCheckedRadioButtonId();

        if (checkedId == R.id.chipCosechado) {
            return "cosechado";
        } else if (checkedId == R.id.chipPerdido) {
            return "perdido";
        } else {
            return "creciendo";
        }
    }
}
