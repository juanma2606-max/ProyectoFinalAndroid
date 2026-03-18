package com.example.proyectofinal.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.proyectofinal.R;
import com.example.proyectofinal.dao.HuertoDAO;
import com.example.proyectofinal.modelos.Huerto;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

public class AgregarHuertoActivity extends AppCompatActivity {

    private TextInputEditText etNombre, etDescripcion;
    private ChipGroup chipGroupTipo;
    private Button btnGuardar, btnCancelar;

    private HuertoDAO huertoDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_huerto);

        huertoDAO = new HuertoDAO();

        etNombre      = findViewById(R.id.etNombreHuerto);
        etDescripcion = findViewById(R.id.etDescripcionHuerto);
        chipGroupTipo = findViewById(R.id.chipGroupTipo);
        btnGuardar    = findViewById(R.id.btnGuardar);
        btnCancelar   = findViewById(R.id.btnCancelar);

        ImageButton btnVolver = findViewById(R.id.btnVolver);
        btnVolver.setOnClickListener(v -> finish());
        btnCancelar.setOnClickListener(v -> finish());
        btnGuardar.setOnClickListener(v -> guardarHuerto());
    }

    private void guardarHuerto() {
        String nombre = etNombre.getText() != null ? etNombre.getText().toString().trim() : "";
        String desc   = etDescripcion.getText() != null ? etDescripcion.getText().toString().trim() : "";
        String tipo   = chipGroupTipo.getCheckedChipId() == R.id.chipMaceta ? "maceta" : "parcela";

        if (nombre.isEmpty()) {
            etNombre.setError("El nombre es obligatorio");
            etNombre.requestFocus();
            return;
        }

        Huerto nuevo = new Huerto(null, nombre, desc, tipo, null);
        btnGuardar.setEnabled(false);

        huertoDAO.createHuerto(nuevo, new HuertoDAO.OnCompleteCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(AgregarHuertoActivity.this, "Huerto creado ✅", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String message) {
                btnGuardar.setEnabled(true);
                Toast.makeText(AgregarHuertoActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}