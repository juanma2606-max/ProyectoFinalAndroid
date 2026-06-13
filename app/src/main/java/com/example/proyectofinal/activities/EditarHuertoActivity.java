package com.example.proyectofinal.activities;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
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

public class EditarHuertoActivity extends AppCompatActivity {

    private TextInputEditText etNombre, etDescripcion, etUbicacion, etSuperficie, etHorasSol, etNotas;
    private Spinner spinnerTipoSuelo;
    private CheckBox cbTieneRiego;
    private Button btnGuardar, btnCancelar, btnSubirFotoHuerto;

    private ImageView imgFoto1, imgFoto2, imgFoto3, imgFotoPropia;
    private CardView cardFoto1, cardFoto2, cardFoto3, cardFotoPropia;
    private String fotoSeleccionada = "huerto1.jpg";

    private final String FOTO_1 = "huerto1.jpg";
    private final String FOTO_2 = "huerto2.webp";
    private final String FOTO_3 = "huerto3.webp";

    private HuertoDAO huertoDAO;
    private String huertoId;
    private String propietarioUid;
    private Huerto huertoActual;

    private ActivityResultLauncher<String> seleccionarFotoLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_huerto);

        huertoDAO = new HuertoDAO();
        huertoId = getIntent().getStringExtra("huertoId");
        propietarioUid = getIntent().getStringExtra("adminUid");

        if (huertoId == null || huertoId.isEmpty()) {
            Toast.makeText(this, "Error: ID inválido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        seleccionarFotoLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> { if (uri != null) subirFotoHuerto(uri); }
        );

        initViews();
        configurarSelectorFotos();
        cargarHuerto();

        ImageButton btnVolver = findViewById(R.id.btnVolver);
        btnVolver.setOnClickListener(v -> finish());
        btnCancelar.setOnClickListener(v -> finish());
        btnGuardar.setOnClickListener(v -> guardarCambios());
        btnSubirFotoHuerto.setOnClickListener(v -> seleccionarFotoLauncher.launch("image/*"));
    }

    private void initViews() {
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
        btnSubirFotoHuerto = findViewById(R.id.btnSubirFotoHuerto);

        imgFoto1 = findViewById(R.id.imgFoto1);
        imgFoto2 = findViewById(R.id.imgFoto2);
        imgFoto3 = findViewById(R.id.imgFoto3);
        imgFotoPropia = findViewById(R.id.imgFotoPropia);
        cardFoto1 = findViewById(R.id.cardFoto1);
        cardFoto2 = findViewById(R.id.cardFoto2);
        cardFoto3 = findViewById(R.id.cardFoto3);
        cardFotoPropia = findViewById(R.id.cardFotoPropia);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.tipos_suelo, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipoSuelo.setAdapter(adapter);
    }

    private void configurarSelectorFotos() {
        imgFoto1.setImageResource(R.drawable.huerto1);
        imgFoto2.setImageResource(R.drawable.huerto2);
        imgFoto3.setImageResource(R.drawable.huerto3);
        imgFotoPropia.setImageResource(R.drawable.ic_planta_placeholder);

        cardFoto1.setOnClickListener(v -> { fotoSeleccionada = FOTO_1; seleccionarCardFoto(cardFoto1); });
        cardFoto2.setOnClickListener(v -> { fotoSeleccionada = FOTO_2; seleccionarCardFoto(cardFoto2); });
        cardFoto3.setOnClickListener(v -> { fotoSeleccionada = FOTO_3; seleccionarCardFoto(cardFoto3); });

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

    private void subirFotoHuerto(Uri uri) {
        SnackbarHelper.show(this, "Subiendo foto...");
        btnSubirFotoHuerto.setEnabled(false);
        imgFotoPropia.setImageURI(uri); // preview local inmediato

        CloudinaryUploader.subirImagen(this, uri, new CloudinaryUploader.OnUploadResult() {
            @Override
            public void onSuccess(String secureUrl) {
                fotoSeleccionada = secureUrl;
                btnSubirFotoHuerto.setEnabled(true);
                Glide.with(EditarHuertoActivity.this).load(secureUrl).centerCrop().into(imgFotoPropia);
                seleccionarCardFoto(cardFotoPropia);
                SnackbarHelper.showSuccess(EditarHuertoActivity.this, "Foto lista ✓");
            }

            @Override
            public void onError(String mensaje) {
                btnSubirFotoHuerto.setEnabled(true);
                imgFotoPropia.setImageResource(R.drawable.ic_planta_placeholder);
                SnackbarHelper.showError(EditarHuertoActivity.this, "Error: " + mensaje);
            }
        });
    }

    private void cargarHuerto() {
        HuertoDAO.OnHuertoLoadedCallback callback = new HuertoDAO.OnHuertoLoadedCallback() {
            @Override
            public void onLoaded(Huerto huerto) {
                huertoActual = huerto;
                mostrarDatos(huerto);
            }
            @Override
            public void onError(Exception e) {
                SnackbarHelper.showError(EditarHuertoActivity.this, "Error al cargar huerto");
                finish();
            }
        };

        if (propietarioUid != null && !propietarioUid.isEmpty()) {
            huertoDAO.getHuertoByUidAndId(propietarioUid, huertoId, callback);
        } else {
            huertoDAO.getHuertoById(huertoId, callback);
        }
    }

    private void mostrarDatos(Huerto huerto) {
        etNombre.setText(huerto.getNombre());
        etDescripcion.setText(huerto.getDescripcion());
        etUbicacion.setText(huerto.getUbicacion());
        etSuperficie.setText(String.valueOf(huerto.getSuperficie()));
        etHorasSol.setText(String.valueOf(huerto.getHoras_sol()));
        cbTieneRiego.setChecked(huerto.getTiene_riego());
        etNotas.setText(huerto.getNotas());

        String tipoSuelo = huerto.getTipo_suelo();
        String[] tipos = getResources().getStringArray(R.array.tipos_suelo);
        for (int i = 0; i < tipos.length; i++) {
            if (tipos[i].equalsIgnoreCase(tipoSuelo)) {
                spinnerTipoSuelo.setSelection(i);
                break;
            }
        }

        // Seleccionar la foto correcta
        if (huerto.getFoto() != null) {
            String foto = huerto.getFoto().replace("/images/", "").trim();
            fotoSeleccionada = foto;

            if (foto.equals(FOTO_1)) {
                seleccionarCardFoto(cardFoto1);
            } else if (foto.equals(FOTO_2)) {
                seleccionarCardFoto(cardFoto2);
            } else if (foto.equals(FOTO_3)) {
                seleccionarCardFoto(cardFoto3);
            } else if (foto.startsWith("http")) {
                // Es una URL de Cloudinary — mostrarla en la card propia
                Glide.with(this).load(foto).centerCrop().into(imgFotoPropia);
                seleccionarCardFoto(cardFotoPropia);
            }
        }
    }

    private void guardarCambios() {
        String nombre = etNombre.getText().toString().trim();
        if (nombre.isEmpty()) { Toast.makeText(this, "El nombre es obligatorio", Toast.LENGTH_SHORT).show(); return; }

        String ubicacion = etUbicacion.getText().toString().trim();
        if (ubicacion.isEmpty()) { Toast.makeText(this, "La ubicación es obligatoria", Toast.LENGTH_SHORT).show(); return; }

        double superficie;
        try {
            superficie = Double.parseDouble(etSuperficie.getText().toString().trim());
            if (superficie < 0.1) { Toast.makeText(this, "Superficie mínima: 0.1 m²", Toast.LENGTH_SHORT).show(); return; }
        } catch (NumberFormatException e) { Toast.makeText(this, "Superficie inválida", Toast.LENGTH_SHORT).show(); return; }

        int horasSol;
        try {
            horasSol = Integer.parseInt(etHorasSol.getText().toString().trim());
            if (horasSol < 0 || horasSol > 24) { Toast.makeText(this, "Horas entre 0 y 24", Toast.LENGTH_SHORT).show(); return; }
        } catch (NumberFormatException e) { Toast.makeText(this, "Horas inválidas", Toast.LENGTH_SHORT).show(); return; }

        huertoActual.setNombre(nombre);
        huertoActual.setDescripcion(etDescripcion.getText().toString().trim());
        huertoActual.setUbicacion(ubicacion);
        huertoActual.setSuperficie(superficie);
        huertoActual.setTipo_suelo(spinnerTipoSuelo.getSelectedItem().toString().toLowerCase());
        huertoActual.setHoras_sol(horasSol);
        huertoActual.setTiene_riego(cbTieneRiego.isChecked());
        huertoActual.setNotas(etNotas.getText().toString().trim());
        huertoActual.setFoto(fotoSeleccionada);

        btnGuardar.setEnabled(false);

        HuertoDAO.OnCompleteCallback callback = new HuertoDAO.OnCompleteCallback() {
            @Override public void onSuccess() {
                SnackbarHelper.showSuccess(EditarHuertoActivity.this, "Huerto actualizado ✅");
                finish();
            }
            @Override public void onError(String message) {
                btnGuardar.setEnabled(true);
                SnackbarHelper.showError(EditarHuertoActivity.this, "Error: " + message);
            }
        };

        if (propietarioUid != null && !propietarioUid.isEmpty()) {
            huertoDAO.updateHuertoForUser(propietarioUid, huertoActual, callback);
        } else {
            huertoDAO.updateHuerto(huertoActual, callback);
        }
    }
}