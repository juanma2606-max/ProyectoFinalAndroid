package com.example.proyectofinal.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.proyectofinal.R;
import com.example.proyectofinal.dao.HuertoDAO;
import com.example.proyectofinal.modelos.Huerto;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AgregarHuertoActivity extends AppCompatActivity {

    private TextInputEditText etNombre;
    private TextInputEditText etDescripcion;
    private TextInputEditText etUbicacion;
    private TextInputEditText etSuperficie;
    private Spinner spinnerTipoSuelo;
    private TextInputEditText etHorasSol;
    private CheckBox cbTieneRiego;
    private TextInputEditText etNotas;
    private Button btnGuardar;
    private Button btnCancelar;

    // Selector de fotos
    private ImageView imgFoto1, imgFoto2, imgFoto3;
    private CardView cardFoto1, cardFoto2, cardFoto3;
    private String fotoSeleccionada = "huerto1.jpg"; // Por defecto

    // URLs de las fotos (mismo que web)
    private final String FOTO_1 = "huerto1.jpg";
    private final String FOTO_2 = "huerto2.webp";
    private final String FOTO_3 = "huerto3.webp";

    private HuertoDAO huertoDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_huerto);

        huertoDAO = new HuertoDAO();

        // Inicializar vistas
        etNombre = findViewById(R.id.etNombreHuerto);
        etDescripcion = findViewById(R.id.etDescripcionHuerto);
        etUbicacion = findViewById(R.id.etUbicacion);
        etSuperficie = findViewById(R.id.etSuperficie);
        spinnerTipoSuelo = findViewById(R.id.spinnerTipoSuelo);
        etHorasSol = findViewById(R.id.etHorasSol);
        cbTieneRiego = findViewById(R.id.cbTieneRiego);
        etNotas = findViewById(R.id.etNotas);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnCancelar = findViewById(R.id.btnCancelar);

        // Selector de fotos
        imgFoto1 = findViewById(R.id.imgFoto1);
        imgFoto2 = findViewById(R.id.imgFoto2);
        imgFoto3 = findViewById(R.id.imgFoto3);
        cardFoto1 = findViewById(R.id.cardFoto1);
        cardFoto2 = findViewById(R.id.cardFoto2);
        cardFoto3 = findViewById(R.id.cardFoto3);

        ImageButton btnVolver = findViewById(R.id.btnVolver);

        // Configurar Spinner tipo suelo
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.tipos_suelo,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipoSuelo.setAdapter(adapter);

        // Valores por defecto
        etHorasSol.setText("6");

        // Configurar selector de fotos
        configurarSelectorFotos();

        // Listeners
        btnVolver.setOnClickListener(v -> finish());
        btnCancelar.setOnClickListener(v -> finish());
        btnGuardar.setOnClickListener(v -> guardarHuerto());
    }

    private void configurarSelectorFotos() {
        // Cargar imágenes (desde drawable)
        imgFoto1.setImageResource(R.drawable.huerto1);
        imgFoto2.setImageResource(R.drawable.huerto2);
        imgFoto3.setImageResource(R.drawable.huerto3);

        // Listeners para selección
        cardFoto1.setOnClickListener(v -> seleccionarFoto(FOTO_1, cardFoto1));
        cardFoto2.setOnClickListener(v -> seleccionarFoto(FOTO_2, cardFoto2));
        cardFoto3.setOnClickListener(v -> seleccionarFoto(FOTO_3, cardFoto3));

        // Seleccionar primera por defecto
        seleccionarFoto(FOTO_1, cardFoto1);
    }

    private void seleccionarFoto(String foto, CardView card) {
        fotoSeleccionada = foto;

        // Resetear todas las cards
        cardFoto1.setCardElevation(4);
        cardFoto2.setCardElevation(4);
        cardFoto3.setCardElevation(4);

        cardFoto1.setAlpha(0.6f);
        cardFoto2.setAlpha(0.6f);
        cardFoto3.setAlpha(0.6f);

        // Destacar la seleccionada
        card.setCardElevation(12);
        card.setAlpha(1.0f);
    }

    private void guardarHuerto() {
        // Obtener valores
        String nombre = etNombre.getText() != null
                ? etNombre.getText().toString().trim()
                : "";

        String descripcion = etDescripcion.getText() != null
                ? etDescripcion.getText().toString().trim()
                : "";

        String ubicacion = etUbicacion.getText() != null
                ? etUbicacion.getText().toString().trim()
                : "";

        String superficieStr = etSuperficie.getText() != null
                ? etSuperficie.getText().toString().trim()
                : "0";

        String tipoSuelo = spinnerTipoSuelo.getSelectedItem().toString();

        String horasSolStr = etHorasSol.getText() != null
                ? etHorasSol.getText().toString().trim()
                : "6";

        boolean tieneRiego = cbTieneRiego.isChecked();

        String notas = etNotas.getText() != null
                ? etNotas.getText().toString().trim()
                : "";

        // Validaciones
        if (nombre.isEmpty()) {
            etNombre.setError("El nombre es obligatorio");
            etNombre.requestFocus();
            return;
        }

        if (nombre.length() < 3) {
            etNombre.setError("El nombre debe tener al menos 3 caracteres");
            etNombre.requestFocus();
            return;
        }

        if (ubicacion.isEmpty()) {
            etUbicacion.setError("La ubicación es obligatoria");
            etUbicacion.requestFocus();
            return;
        }

        double superficie;
        try {
            superficie = Double.parseDouble(superficieStr);
            if (superficie < 0.1) {
                etSuperficie.setError("La superficie debe ser al menos 0.1 m²");
                etSuperficie.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            etSuperficie.setError("Superficie inválida");
            etSuperficie.requestFocus();
            return;
        }

        int horasSol;
        try {
            horasSol = Integer.parseInt(horasSolStr);
            if (horasSol < 0 || horasSol > 24) {
                etHorasSol.setError("Las horas de sol deben estar entre 0 y 24");
                etHorasSol.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            etHorasSol.setError("Horas de sol inválidas");
            etHorasSol.requestFocus();
            return;
        }

        // Fecha actual en formato ISO
        String fechaISO = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                .format(new Date());

        // Crear objeto Huerto con foto seleccionada
        Huerto nuevo = new Huerto(
                null,                // id (Firebase lo genera)
                nombre,              // nombre
                descripcion,         // descripcion
                fotoSeleccionada,    // foto ← AQUÍ VA LA FOTO SELECCIONADA
                ubicacion,           // ubicacion
                superficie,          // superficie
                tipoSuelo,           // tipo_suelo
                horasSol,            // horas_sol
                tieneRiego,          // tiene_riego
                notas,               // notas
                fechaISO             // fecha_creacion
        );

        btnGuardar.setEnabled(false);

        huertoDAO.createHuerto(nuevo, new HuertoDAO.OnCompleteCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(AgregarHuertoActivity.this,
                        "Huerto creado ✅", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String message) {
                btnGuardar.setEnabled(true);
                Toast.makeText(AgregarHuertoActivity.this,
                        "Error: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}