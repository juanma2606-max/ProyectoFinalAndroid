package com.example.proyectofinal.activities;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.proyectofinal.R;
import com.example.proyectofinal.dao.HuertoDAO;
import com.example.proyectofinal.modelos.Huerto;
import com.example.proyectofinal.utils.SnackbarHelper;
import com.google.android.material.textfield.TextInputEditText;

public class EditarHuertoActivity extends AppCompatActivity {

    private TextInputEditText etNombre, etDescripcion, etUbicacion, etSuperficie, etHorasSol, etNotas;
    private Spinner spinnerTipoSuelo;
    private CheckBox cbTieneRiego;
    private Button btnGuardar, btnCancelar;

    private ImageView imgFoto1, imgFoto2, imgFoto3;
    private CardView cardFoto1, cardFoto2, cardFoto3;
    private String fotoSeleccionada = "huerto1.jpg";

    private final String FOTO_1 = "huerto1.jpg";
    private final String FOTO_2 = "huerto2.webp";
    private final String FOTO_3 = "huerto3.webp";

    private HuertoDAO huertoDAO;
    private String huertoId;
    private String propietarioUid;
    private Huerto huertoActual;

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

        initViews();
        configurarSelectorFotos();
        cargarHuerto();

        ImageButton btnVolver = findViewById(R.id.btnVolver);
        btnVolver.setOnClickListener(v -> finish());
        btnCancelar.setOnClickListener(v -> finish());
        btnGuardar.setOnClickListener(v -> guardarCambios());
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

        imgFoto1 = findViewById(R.id.imgFoto1);
        imgFoto2 = findViewById(R.id.imgFoto2);
        imgFoto3 = findViewById(R.id.imgFoto3);
        cardFoto1 = findViewById(R.id.cardFoto1);
        cardFoto2 = findViewById(R.id.cardFoto2);
        cardFoto3 = findViewById(R.id.cardFoto3);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.tipos_suelo,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipoSuelo.setAdapter(adapter);
    }

    private void configurarSelectorFotos() {
        imgFoto1.setImageResource(R.drawable.huerto1);
        imgFoto2.setImageResource(R.drawable.huerto2);
        imgFoto3.setImageResource(R.drawable.huerto3);

        cardFoto1.setOnClickListener(v -> seleccionarFoto(FOTO_1, cardFoto1));
        cardFoto2.setOnClickListener(v -> seleccionarFoto(FOTO_2, cardFoto2));
        cardFoto3.setOnClickListener(v -> seleccionarFoto(FOTO_3, cardFoto3));

        seleccionarFoto(FOTO_1, cardFoto1);
    }

    private void seleccionarFoto(String foto, CardView card) {
        fotoSeleccionada = foto;

        cardFoto1.setCardElevation(4);
        cardFoto2.setCardElevation(4);
        cardFoto3.setCardElevation(4);

        cardFoto1.setAlpha(0.6f);
        cardFoto2.setAlpha(0.6f);
        cardFoto3.setAlpha(0.6f);

        card.setCardElevation(12);
        card.setAlpha(1.0f);
    }

    private void cargarHuerto() {
        if (propietarioUid != null && !propietarioUid.isEmpty()) {
            huertoDAO.getHuertoByUidAndId(propietarioUid, huertoId, new HuertoDAO.OnHuertoLoadedCallback() {
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
            });
        } else {
            huertoDAO.getHuertoById(huertoId, new HuertoDAO.OnHuertoLoadedCallback() {
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
            });
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

        if (huerto.getFoto() != null) {
            String foto = huerto.getFoto().replace("/images/", "").trim();

            CardView cardSeleccionar = null;
            if (foto.equals(FOTO_1)) cardSeleccionar = cardFoto1;
            else if (foto.equals(FOTO_2)) cardSeleccionar = cardFoto2;
            else if (foto.equals(FOTO_3)) cardSeleccionar = cardFoto3;

            if (cardSeleccionar != null) {
                seleccionarFoto(foto, cardSeleccionar);
            }
        }
    }

    private void guardarCambios() {
        String nombre = etNombre.getText().toString().trim();
        if (nombre.isEmpty()) {
            Toast.makeText(this, "El nombre es obligatorio", Toast.LENGTH_SHORT).show();
            return;
        }

        String ubicacion = etUbicacion.getText().toString().trim();
        if (ubicacion.isEmpty()) {
            Toast.makeText(this, "La ubicación es obligatoria", Toast.LENGTH_SHORT).show();
            return;
        }

        double superficie;
        try {
            superficie = Double.parseDouble(etSuperficie.getText().toString().trim());
            if (superficie < 0.1) {
                Toast.makeText(this, "Superficie mínima: 0.1 m²", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Superficie inválida", Toast.LENGTH_SHORT).show();
            return;
        }

        int horasSol;
        try {
            horasSol = Integer.parseInt(etHorasSol.getText().toString().trim());
            if (horasSol < 0 || horasSol > 24) {
                Toast.makeText(this, "Horas entre 0 y 24", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Horas inválidas", Toast.LENGTH_SHORT).show();
            return;
        }

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

        if (propietarioUid != null && !propietarioUid.isEmpty()) {
            huertoDAO.updateHuertoForUser(propietarioUid, huertoActual, new HuertoDAO.OnCompleteCallback() {
                @Override public void onSuccess() {
                    SnackbarHelper.showSuccess(EditarHuertoActivity.this, "Huerto actualizado ✅");
                    finish();
                }
                @Override public void onError(String message) {
                    btnGuardar.setEnabled(true);
                    SnackbarHelper.showError(EditarHuertoActivity.this, "Error: " + message);
                }
            });
        } else {
            huertoDAO.updateHuerto(huertoActual, new HuertoDAO.OnCompleteCallback() {
                @Override public void onSuccess() {
                    SnackbarHelper.showSuccess(EditarHuertoActivity.this, "Huerto actualizado ✅");
                    finish();
                }
                @Override public void onError(String message) {
                    btnGuardar.setEnabled(true);
                    SnackbarHelper.showError(EditarHuertoActivity.this, "Error: " + message);
                }
            });
        }
    }
}