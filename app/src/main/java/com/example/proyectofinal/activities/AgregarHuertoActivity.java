package com.example.proyectofinal.activities;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.example.proyectofinal.R;
import com.example.proyectofinal.dao.HuertoDAO;
import com.example.proyectofinal.modelos.Huerto;
import com.example.proyectofinal.utils.CloudinaryUploader;
import com.example.proyectofinal.utils.SnackbarHelper;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AgregarHuertoActivity extends AppCompatActivity {

    private TextInputEditText etNombre, etDescripcion, etUbicacion, etSuperficie, etHorasSol, etNotas;
    private Spinner spinnerTipoSuelo;
    private CheckBox cbTieneRiego;
    private Button btnGuardar, btnCancelar;

    private ImageView imgFoto1, imgFoto2, imgFoto3, imgFotoPropia;
    private CardView cardFoto1, cardFoto2, cardFoto3, cardFotoPropia;
    private Button btnSubirFotoHuerto;

    private String fotoSeleccionada = "huerto1.jpg";
    private final String FOTO_1 = "huerto1.jpg";
    private final String FOTO_2 = "huerto2.webp";
    private final String FOTO_3 = "huerto3.webp";

    private HuertoDAO huertoDAO;
    private ActivityResultLauncher<String> seleccionarFotoLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_huerto);

        huertoDAO = new HuertoDAO();

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

        imgFoto1 = findViewById(R.id.imgFoto1);
        imgFoto2 = findViewById(R.id.imgFoto2);
        imgFoto3 = findViewById(R.id.imgFoto3);
        cardFoto1 = findViewById(R.id.cardFoto1);
        cardFoto2 = findViewById(R.id.cardFoto2);
        cardFoto3 = findViewById(R.id.cardFoto3);

        // Nuevas vistas para foto propia
        imgFotoPropia = findViewById(R.id.imgFotoPropia);
        cardFotoPropia = findViewById(R.id.cardFotoPropia);
        btnSubirFotoHuerto = findViewById(R.id.btnSubirFotoHuerto);

        ImageButton btnVolver = findViewById(R.id.btnVolver);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.tipos_suelo, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipoSuelo.setAdapter(adapter);

        seleccionarFotoLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> { if (uri != null) subirFotoHuerto(uri); }
        );

        configurarSelectorFotos();

        btnVolver.setOnClickListener(v -> finish());
        btnCancelar.setOnClickListener(v -> finish());
        btnGuardar.setOnClickListener(v -> guardarHuerto());
        btnSubirFotoHuerto.setOnClickListener(v -> seleccionarFotoLauncher.launch("image/*"));
    }

    private void subirFotoHuerto(Uri uri) {
        SnackbarHelper.show(this, "Subiendo foto...");
        btnSubirFotoHuerto.setEnabled(false);

        // Preview local inmediato
        imgFotoPropia.setImageURI(uri);

        CloudinaryUploader.subirImagen(this, uri, new CloudinaryUploader.OnUploadResult() {
            @Override
            public void onSuccess(String secureUrl) {
                fotoSeleccionada = secureUrl;
                btnSubirFotoHuerto.setEnabled(true);

                // Mostrar la card de foto propia y seleccionarla
                Glide.with(AgregarHuertoActivity.this).load(secureUrl).centerCrop().into(imgFotoPropia);
                seleccionarCardFoto(cardFotoPropia);
                SnackbarHelper.showSuccess(AgregarHuertoActivity.this, "Foto lista ✓");
            }

            @Override
            public void onError(String mensaje) {
                btnSubirFotoHuerto.setEnabled(true);
                imgFotoPropia.setImageResource(R.drawable.ic_planta_placeholder);
                SnackbarHelper.showError(AgregarHuertoActivity.this, "Error: " + mensaje);
            }
        });
    }

    private void configurarSelectorFotos() {
        imgFoto1.setImageResource(R.drawable.huerto1);
        imgFoto2.setImageResource(R.drawable.huerto2);
        imgFoto3.setImageResource(R.drawable.huerto3);
        imgFotoPropia.setImageResource(R.drawable.ic_planta_placeholder);

        cardFoto1.setOnClickListener(v -> { fotoSeleccionada = FOTO_1; seleccionarCardFoto(cardFoto1); });
        cardFoto2.setOnClickListener(v -> { fotoSeleccionada = FOTO_2; seleccionarCardFoto(cardFoto2); });
        cardFoto3.setOnClickListener(v -> { fotoSeleccionada = FOTO_3; seleccionarCardFoto(cardFoto3); });
        // cardFotoPropia no cambia fotoSeleccionada al tocarla sola; solo se activa tras subir

        seleccionarCardFoto(cardFoto1);
    }

    private void seleccionarCardFoto(CardView seleccionada) {
        CardView[] todas = {cardFoto1, cardFoto2, cardFoto3, cardFotoPropia};
        for (CardView c : todas) {
            c.setCardElevation(4);
            c.setAlpha(0.6f);
        }
        seleccionada.setCardElevation(12);
        seleccionada.setAlpha(1.0f);
    }

    private void guardarHuerto() {
        String nombre = etNombre.getText() != null ? etNombre.getText().toString().trim() : "";
        String descripcion = etDescripcion.getText() != null ? etDescripcion.getText().toString().trim() : "";
        String ubicacion = etUbicacion.getText() != null ? etUbicacion.getText().toString().trim() : "";
        String superficieStr = etSuperficie.getText() != null ? etSuperficie.getText().toString().trim() : "0";
        String tipoSuelo = spinnerTipoSuelo.getSelectedItem().toString();
        String horasSolStr = etHorasSol.getText() != null ? etHorasSol.getText().toString().trim() : "6";
        boolean tieneRiego = cbTieneRiego.isChecked();
        String notas = etNotas.getText() != null ? etNotas.getText().toString().trim() : "";

        if (nombre.isEmpty()) { etNombre.setError("El nombre es obligatorio"); etNombre.requestFocus(); return; }
        if (nombre.length() < 3) { etNombre.setError("Mínimo 3 caracteres"); etNombre.requestFocus(); return; }
        if (ubicacion.isEmpty()) { etUbicacion.setError("La ubicación es obligatoria"); etUbicacion.requestFocus(); return; }

        double superficie;
        try {
            superficie = Double.parseDouble(superficieStr);
            if (superficie < 0.1) { etSuperficie.setError("Mínimo 0.1 m²"); etSuperficie.requestFocus(); return; }
        } catch (NumberFormatException e) { etSuperficie.setError("Superficie inválida"); etSuperficie.requestFocus(); return; }

        int horasSol;
        try {
            horasSol = Integer.parseInt(horasSolStr);
            if (horasSol < 0 || horasSol > 24) { etHorasSol.setError("Entre 0 y 24"); etHorasSol.requestFocus(); return; }
        } catch (NumberFormatException e) { etHorasSol.setError("Horas inválidas"); etHorasSol.requestFocus(); return; }

        String fechaISO = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).format(new Date());

        Huerto nuevo = new Huerto(null, nombre, descripcion, fotoSeleccionada,
                ubicacion, superficie, tipoSuelo, horasSol, tieneRiego, notas, fechaISO);

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