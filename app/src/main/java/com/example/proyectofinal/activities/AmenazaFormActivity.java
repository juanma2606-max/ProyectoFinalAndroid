package com.example.proyectofinal.activities;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.proyectofinal.R;
import com.example.proyectofinal.dao.AmenazaDAO;
import com.example.proyectofinal.modelos.Amenaza;
import com.example.proyectofinal.utils.CloudinaryUploader;
import com.example.proyectofinal.utils.SnackbarHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class AmenazaFormActivity extends AppCompatActivity {

    private TextInputEditText etNombre, etDescripcion, etImagen, etTratamiento;
    private Spinner spinnerTipo;
    private LinearLayout listaSintomas;
    private MaterialButton btnAgregarSintoma, btnGuardar, btnCancelar;
    private MaterialButton btnSubirImagen;
    private ImageView imgPreview;
    private ProgressBar progressSubidaImagen;
    private ActivityResultLauncher<String> seleccionarImagenLauncher;

    private AmenazaDAO amenazaDAO;
    private List<String> sintomas = new ArrayList<>();
    private String amenazaId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_amenaza_form);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        amenazaDAO = new AmenazaDAO();
        amenazaId = getIntent().getStringExtra("amenazaId");

        // Selector de imagen de galería -> subida a Cloudinary
        seleccionarImagenLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        subirImagenSeleccionada(uri);
                    }
                }
        );

        initViews();

        TextView txtTitulo = findViewById(R.id.txtTituloAmenazaForm);
        if (amenazaId != null) {
            txtTitulo.setText("Editar amenaza");
            cargarAmenaza();
        } else {
            txtTitulo.setText("Nueva amenaza");
        }

        btnGuardar.setOnClickListener(v -> guardar());
        btnCancelar.setOnClickListener(v -> finish());
        btnAgregarSintoma.setOnClickListener(v -> agregarSintoma());
    }

    private void initViews() {
        etNombre = findViewById(R.id.etAmenazaNombre);
        etDescripcion = findViewById(R.id.etAmenazaDescripcion);
        etImagen = findViewById(R.id.etAmenazaImagen);
        etTratamiento = findViewById(R.id.etAmenazaTratamiento);
        spinnerTipo = findViewById(R.id.spinnerAmenazaTipo);
        listaSintomas = findViewById(R.id.listaSintomas);
        btnAgregarSintoma = findViewById(R.id.btnAgregarSintoma);
        btnGuardar = findViewById(R.id.btnAmenazaGuardar);
        btnCancelar = findViewById(R.id.btnAmenazaCancelar);

        btnSubirImagen = findViewById(R.id.btnSubirImagenAmenaza);
        imgPreview = findViewById(R.id.imgPreviewAmenaza);
        progressSubidaImagen = findViewById(R.id.progressSubidaImagen);

        btnSubirImagen.setOnClickListener(v -> seleccionarImagenLauncher.launch("image/*"));

        ArrayAdapter<String> tipoAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"plaga", "enfermedad"});
        tipoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipo.setAdapter(tipoAdapter);
    }

    private void cargarAmenaza() {
        amenazaDAO.getAmenazaById(amenazaId, new AmenazaDAO.OnAmenazaLoadedCallback() {
            @Override
            public void onLoaded(Amenaza amenaza) {
                etNombre.setText(amenaza.getNombre());
                etDescripcion.setText(amenaza.getDescripcion());
                etImagen.setText(amenaza.getImagen());
                imgPreview.setVisibility(View.VISIBLE);
                if (amenaza.getImagen() != null && !amenaza.getImagen().isEmpty()) {
                    Picasso.get()
                            .load(amenaza.getImagen())
                            .placeholder(R.drawable.ic_bug)
                            .error(R.drawable.ic_bug)
                            .into(imgPreview, new com.squareup.picasso.Callback() {
                                @Override
                                public void onSuccess() {
                                    android.util.Log.d("AMENAZA_IMG", "Preview cargado: " + amenaza.getImagen());
                                }

                                @Override
                                public void onError(Exception e) {
                                    android.util.Log.e("AMENAZA_IMG", "Error cargando preview: " + amenaza.getImagen(), e);
                                }
                            });
                } else {
                    imgPreview.setImageResource(R.drawable.ic_bug);
                }
                etTratamiento.setText(amenaza.getTratamiento());

                if ("enfermedad".equals(amenaza.getTipo())) {
                    spinnerTipo.setSelection(1);
                }

                sintomas.clear();
                sintomas.addAll(amenaza.getSintomas());
                refrescarSintomas();
            }
            @Override
            public void onError(Exception e) {
                SnackbarHelper.showError(AmenazaFormActivity.this, "Error al cargar amenaza");
                finish();
            }
        });
    }

    // ---------------------------------------------------------
    // Sube la imagen elegida de la galería a Cloudinary y rellena
    // el campo etImagen con la URL resultante
    // ---------------------------------------------------------
    private void subirImagenSeleccionada(Uri uri) {
        // Vista previa inmediata con la imagen local
        imgPreview.setVisibility(View.VISIBLE);
        imgPreview.setImageURI(uri);

        progressSubidaImagen.setVisibility(View.VISIBLE);
        btnSubirImagen.setEnabled(false);

        CloudinaryUploader.subirImagen(this, uri, new CloudinaryUploader.OnUploadResult() {
            @Override
            public void onSuccess(String secureUrl) {
                progressSubidaImagen.setVisibility(View.GONE);
                btnSubirImagen.setEnabled(true);
                etImagen.setText(secureUrl);
                SnackbarHelper.showSuccess(AmenazaFormActivity.this, "Imagen subida ✅");
            }

            @Override
            public void onError(String mensaje) {
                progressSubidaImagen.setVisibility(View.GONE);
                btnSubirImagen.setEnabled(true);
                SnackbarHelper.showError(AmenazaFormActivity.this, "Error al subir imagen: " + mensaje);
            }
        });
    }

    private void agregarSintoma() {
        android.widget.EditText et = new android.widget.EditText(this);
        et.setHint("Escribe un síntoma");
        et.setPadding(48, 24, 48, 24);

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Añadir síntoma")
                .setView(et)
                .setPositiveButton("Añadir", (d, w) -> {
                    String sintoma = et.getText().toString().trim();
                    if (!sintoma.isEmpty()) {
                        sintomas.add(sintoma);
                        refrescarSintomas();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void refrescarSintomas() {
        listaSintomas.removeAllViews();
        for (String sintoma : new ArrayList<>(sintomas)) {
            View chip = getLayoutInflater().inflate(R.layout.item_incompatibilidad_chip,
                    listaSintomas, false);
            TextView txt = chip.findViewById(R.id.txtChipNombre);
            MaterialButton btnQuitar = chip.findViewById(R.id.btnQuitarChip);
            txt.setText(sintoma);
            btnQuitar.setOnClickListener(v -> {
                sintomas.remove(sintoma);
                refrescarSintomas();
            });
            listaSintomas.addView(chip);
        }
    }

    private void guardar() {
        String nombre = etNombre.getText() != null ? etNombre.getText().toString().trim() : "";
        String descripcion = etDescripcion.getText() != null ? etDescripcion.getText().toString().trim() : "";

        if (nombre.isEmpty()) { SnackbarHelper.showError(this, "El nombre es obligatorio"); return; }
        if (descripcion.isEmpty()) { SnackbarHelper.showError(this, "La descripción es obligatoria"); return; }

        Amenaza amenaza = new Amenaza();
        amenaza.setNombre(nombre);
        amenaza.setDescripcion(descripcion);
        amenaza.setTipo(spinnerTipo.getSelectedItem().toString());
        amenaza.setImagen(etImagen.getText() != null ? etImagen.getText().toString().trim() : "");
        amenaza.setTratamiento(etTratamiento.getText() != null ? etTratamiento.getText().toString().trim() : "");
        amenaza.setSintomas(sintomas);

        btnGuardar.setEnabled(false);

        if (amenazaId != null) {
            amenaza.setId(amenazaId);
            amenazaDAO.updateAmenaza(amenaza, new AmenazaDAO.OnOperationCallback() {
                @Override public void onSuccess() {
                    SnackbarHelper.showSuccess(AmenazaFormActivity.this, "Amenaza actualizada ✅");
                    finish();
                }
                @Override public void onError(Exception e) {
                    btnGuardar.setEnabled(true);
                    SnackbarHelper.showError(AmenazaFormActivity.this, "Error al guardar");
                }
            });
        } else {
            amenazaDAO.createAmenaza(amenaza, new AmenazaDAO.OnOperationCallback() {
                @Override public void onSuccess() {
                    SnackbarHelper.showSuccess(AmenazaFormActivity.this, "Amenaza creada ✅");
                    finish();
                }
                @Override public void onError(Exception e) {
                    btnGuardar.setEnabled(true);
                    SnackbarHelper.showError(AmenazaFormActivity.this, "Error al crear");
                }
            });
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}