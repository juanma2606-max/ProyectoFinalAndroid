package com.example.proyectofinal.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.proyectofinal.R;
import com.example.proyectofinal.dao.PlantaDAO;
import com.example.proyectofinal.modelos.Planta;
import com.example.proyectofinal.utils.SnackbarHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class PlantaFormActivity extends AppCompatActivity {

    private TextInputEditText etNombre, etNombreCientifico, etDescripcion;
    private TextInputEditText etImagen, etAbono, etTiempoCrecimiento;
    private Spinner spinnerTipo, spinnerRiego, spinnerLuz;
    private MaterialCheckBox cbPrimavera, cbVerano, cbOtono, cbInvierno;
    private LinearLayout listaIncompatibilidades;
    private MaterialButton btnAgregarIncompat, btnGuardar, btnCancelar;

    private PlantaDAO plantaDAO;
    private List<Planta> todasPlantas = new ArrayList<>();
    private List<String> incompatibilidadesSeleccionadas = new ArrayList<>();
    private String plantaId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_planta_form);

        plantaDAO = new PlantaDAO();
        plantaId = getIntent().getStringExtra("plantaId");

        initViews();
        cargarTodasPlantas();

        TextView txtTitulo = findViewById(R.id.txtTituloPlantaForm);
        if (plantaId != null) {
            txtTitulo.setText("Editar planta");
            cargarPlanta(); // carga todasPlantas internamente
        } else {
            txtTitulo.setText("Nueva planta");
            cargarTodasPlantas(); // solo para el selector de incompatibilidades
        }

        btnGuardar.setOnClickListener(v -> guardar());
        btnCancelar.setOnClickListener(v -> finish());
        btnAgregarIncompat.setOnClickListener(v -> mostrarSelectorIncompatibilidad());
    }

    private void initViews() {
        etNombre = findViewById(R.id.etPlantaNombre);
        etNombreCientifico = findViewById(R.id.etPlantaNombreCientifico);
        etDescripcion = findViewById(R.id.etPlantaDescripcion);
        etImagen = findViewById(R.id.etPlantaImagen);
        etAbono = findViewById(R.id.etPlantaAbono);
        etTiempoCrecimiento = findViewById(R.id.etPlantaTiempo);
        spinnerTipo = findViewById(R.id.spinnerPlantaTipo);
        spinnerRiego = findViewById(R.id.spinnerPlantaRiego);
        spinnerLuz = findViewById(R.id.spinnerPlantaLuz);
        cbPrimavera = findViewById(R.id.cbPrimavera);
        cbVerano = findViewById(R.id.cbVerano);
        cbOtono = findViewById(R.id.cbOtono);
        cbInvierno = findViewById(R.id.cbInvierno);
        listaIncompatibilidades = findViewById(R.id.listaIncompatibilidades);
        btnAgregarIncompat = findViewById(R.id.btnAgregarIncompat);
        btnGuardar = findViewById(R.id.btnPlantaGuardar);
        btnCancelar = findViewById(R.id.btnPlantaCancelar);

        // Spinners
        ArrayAdapter<String> tipoAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"hortaliza", "fruta", "arbol", "hierba", "flor", "legumbre"});
        tipoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipo.setAdapter(tipoAdapter);

        ArrayAdapter<String> riegoAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"bajo", "moderado", "alto"});
        riegoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRiego.setAdapter(riegoAdapter);

        ArrayAdapter<String> luzAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"pleno-sol", "semi-sombra", "sombra"});
        luzAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLuz.setAdapter(luzAdapter);
    }

    private void cargarTodasPlantas() {
        plantaDAO.getAllPlantasOnce(new PlantaDAO.OnPlantasLoadedCallback() {
            @Override
            public void onLoaded(List<Planta> plantas) {
                todasPlantas = plantas;
            }
            @Override
            public void onError(Exception e) {}
        });
    }

    private void cargarPlanta() {
        plantaDAO.getAllPlantasOnce(new PlantaDAO.OnPlantasLoadedCallback() {
            @Override
            public void onLoaded(List<Planta> plantas) {
                todasPlantas = plantas;

                plantaDAO.getPlantaById(plantaId, new PlantaDAO.OnPlantaLoadedCallback() {
                    @Override
                    public void onLoaded(Planta planta) {
                        etNombre.setText(planta.getNombre());
                        etNombreCientifico.setText(planta.getNombreCientifico());
                        etDescripcion.setText(planta.getDescripcion());
                        etImagen.setText(planta.getImagen());
                        etAbono.setText(planta.getAbono());
                        etTiempoCrecimiento.setText(String.valueOf(planta.getTiempoCrecimiento()));

                        seleccionarEnSpinner(spinnerTipo, planta.getTipo());
                        seleccionarEnSpinner(spinnerRiego, planta.getRiego());
                        seleccionarEnSpinner(spinnerLuz, planta.getLuz());

                        if (planta.getEstacion() != null) {
                            String est = planta.getEstacion().toLowerCase();
                            cbPrimavera.setChecked(est.contains("primavera"));
                            cbVerano.setChecked(est.contains("verano"));
                            cbOtono.setChecked(est.contains("otoño") || est.contains("otono"));
                            cbInvierno.setChecked(est.contains("invierno"));
                        }

                        incompatibilidadesSeleccionadas.clear();
                        incompatibilidadesSeleccionadas.addAll(planta.getIncompatibilidades());
                        refrescarIncompatibilidades();
                    }
                    @Override
                    public void onError(Exception e) {
                        SnackbarHelper.showError(PlantaFormActivity.this, "Error al cargar planta");
                        finish();
                    }
                });
            }
            @Override
            public void onError(Exception e) {
                SnackbarHelper.showError(PlantaFormActivity.this, "Error al cargar plantas");
                finish();
            }
        });
    }
    private void mostrarSelectorIncompatibilidad() {
        List<String> nombres = new ArrayList<>();
        List<String> ids = new ArrayList<>();

        for (Planta p : todasPlantas) {
            if (!incompatibilidadesSeleccionadas.contains(p.getId())
                    && (plantaId == null || !plantaId.equals(p.getId()))) {
                nombres.add(p.getNombre());
                ids.add(p.getId());
            }
        }

        if (nombres.isEmpty()) {
            SnackbarHelper.show(this, "No hay más plantas disponibles");
            return;
        }

        String[] opciones = nombres.toArray(new String[0]);
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Seleccionar planta incompatible")
                .setItems(opciones, (d, which) -> {
                    incompatibilidadesSeleccionadas.add(ids.get(which));
                    refrescarIncompatibilidades();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void refrescarIncompatibilidades() {
        listaIncompatibilidades.removeAllViews();
        for (String id : incompatibilidadesSeleccionadas) {
            String nombre = getNombrePlanta(id);
            View chip = getLayoutInflater().inflate(R.layout.item_incompatibilidad_chip,
                    listaIncompatibilidades, false);
            TextView txt = chip.findViewById(R.id.txtChipNombre);
            MaterialButton btnQuitar = chip.findViewById(R.id.btnQuitarChip);
            txt.setText(nombre);
            btnQuitar.setOnClickListener(v -> {
                incompatibilidadesSeleccionadas.remove(id);
                refrescarIncompatibilidades();
            });
            listaIncompatibilidades.addView(chip);
        }
    }

    private String getNombrePlanta(String id) {
        for (Planta p : todasPlantas) {
            if (id.equals(p.getId())) return p.getNombre();
        }
        return id;
    }

    private void seleccionarEnSpinner(Spinner spinner, String valor) {
        if (valor == null) return;
        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).toString().equals(valor)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    private String getEstacion() {
        List<String> estaciones = new ArrayList<>();
        if (cbPrimavera.isChecked()) estaciones.add("primavera");
        if (cbVerano.isChecked()) estaciones.add("verano");
        if (cbOtono.isChecked()) estaciones.add("otoño");
        if (cbInvierno.isChecked()) estaciones.add("invierno");
        return String.join(", ", estaciones);
    }

    private void guardar() {
        String nombre = etNombre.getText() != null ? etNombre.getText().toString().trim() : "";
        String descripcion = etDescripcion.getText() != null ? etDescripcion.getText().toString().trim() : "";
        String abono = etAbono.getText() != null ? etAbono.getText().toString().trim() : "";
        String tiempoStr = etTiempoCrecimiento.getText() != null ? etTiempoCrecimiento.getText().toString().trim() : "0";

        if (nombre.isEmpty()) { SnackbarHelper.showError(this, "El nombre es obligatorio"); return; }
        if (descripcion.isEmpty()) { SnackbarHelper.showError(this, "La descripción es obligatoria"); return; }
        if (abono.isEmpty()) { SnackbarHelper.showError(this, "El abono es obligatorio"); return; }

        int tiempo;
        try { tiempo = Integer.parseInt(tiempoStr); }
        catch (NumberFormatException e) { SnackbarHelper.showError(this, "Tiempo inválido"); return; }

        Planta planta = new Planta();
        planta.setNombre(nombre);
        planta.setNombreCientifico(etNombreCientifico.getText() != null
                ? etNombreCientifico.getText().toString().trim() : "");
        planta.setDescripcion(descripcion);
        planta.setTipo(spinnerTipo.getSelectedItem().toString());
        planta.setImagen(etImagen.getText() != null ? etImagen.getText().toString().trim() : "");
        planta.setEstacion(getEstacion());
        planta.setTiempoCrecimiento(tiempo);
        planta.setRiego(spinnerRiego.getSelectedItem().toString());
        planta.setLuz(spinnerLuz.getSelectedItem().toString());
        planta.setAbono(abono);
        planta.setIncompatibilidades(incompatibilidadesSeleccionadas);

        btnGuardar.setEnabled(false);

        if (plantaId != null) {
            planta.setId(plantaId);
            plantaDAO.updatePlanta(planta, new PlantaDAO.OnOperationCallback() {
                @Override public void onSuccess() {
                    SnackbarHelper.showSuccess(PlantaFormActivity.this, "Planta actualizada ✅");
                    finish();
                }
                @Override public void onError(Exception e) {
                    btnGuardar.setEnabled(true);
                    SnackbarHelper.showError(PlantaFormActivity.this, "Error al guardar");
                }
            });
        } else {
            plantaDAO.createPlanta(planta, new PlantaDAO.OnOperationCallback() {
                @Override public void onSuccess() {
                    SnackbarHelper.showSuccess(PlantaFormActivity.this, "Planta creada ✅");
                    finish();
                }
                @Override public void onError(Exception e) {
                    btnGuardar.setEnabled(true);
                    SnackbarHelper.showError(PlantaFormActivity.this, "Error al crear");
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