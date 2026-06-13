package com.example.proyectofinal.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.proyectofinal.R;
import com.example.proyectofinal.modelos.Planta;
import com.example.proyectofinal.dao.PlantaDAO;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class PlantaDetalleActivity extends AppCompatActivity {

    private ImageView imgPlanta;
    private TextView txtNombre, txtDescripcion, txtEstacion, txtAbono, txtRiego, txtTiempo;
    private LinearLayout listIncompatibilidades, listPlagas;

    private PlantaDAO plantasService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_planta_detalle);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Detalle de planta");
        }

        plantasService = new PlantaDAO();

        imgPlanta              = findViewById(R.id.imgPlantaDetalle);
        txtNombre              = findViewById(R.id.txtNombreDetalle);
        txtDescripcion         = findViewById(R.id.txtDescripcionDetalle);
        txtEstacion            = findViewById(R.id.txtEstacion);
        txtAbono               = findViewById(R.id.txtAbono);
        txtRiego               = findViewById(R.id.txtRiego);
        txtTiempo              = findViewById(R.id.txtTiempo);
        listIncompatibilidades = findViewById(R.id.listIncompatibilidades);
        listPlagas             = findViewById(R.id.listPlagas);

        ImageButton btnVolver = findViewById(R.id.btnVolver);
        btnVolver.setOnClickListener(v -> finish());

        String id = getIntent().getStringExtra("id");
        cargarPlanta(id);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void cargarPlanta(String id) {
        plantasService.getPlantaById(id, new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Planta planta = snapshot.getValue(Planta.class);
                if (planta == null) return;

                txtNombre.setText(planta.nombre);
                txtDescripcion.setText(planta.descripcion);
                txtEstacion.setText("Estación: " + planta.estacion);
                txtAbono.setText("Abono recomendado: " + planta.abono);
                txtRiego.setText("Riego: " + planta.riego);
                txtTiempo.setText("Tiempo de crecimiento: " + planta.tiempo_crecimiento);

                // Imagen: primero URL de Firebase, si no hay, por tipo
                if (planta.getImagen() != null && !planta.getImagen().isEmpty()) {
                    Picasso.get()
                            .load(planta.getImagen())
                            .placeholder(R.drawable.ic_planta_placeholder)
                            .error(R.drawable.ic_planta_placeholder)
                            .into(imgPlanta);
                } else {
                    cargarImagenPorTipo(imgPlanta, planta.tipo);
                }

                // Incompatibilidades — resolver nombres desde Firebase
                listIncompatibilidades.removeAllViews();
                if (planta.incompatibilidades != null && !planta.incompatibilidades.isEmpty()) {
                    for (String incompId : planta.incompatibilidades) {
                        plantasService.getPlantaById(incompId, new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot snap) {
                                Planta p = snap.getValue(Planta.class);
                                TextView t = new TextView(PlantaDetalleActivity.this);
                                t.setText("• " + (p != null ? p.nombre : incompId));
                                t.setTextSize(16);
                                listIncompatibilidades.addView(t);
                            }
                            @Override public void onCancelled(DatabaseError e) {}
                        });
                    }
                } else {
                    TextView t = new TextView(PlantaDetalleActivity.this);
                    t.setText("Sin incompatibilidades registradas");
                    t.setTextSize(14);
                    t.setAlpha(0.6f);
                    listIncompatibilidades.addView(t);
                }

                // Plagas
                listPlagas.removeAllViews();
                if (planta.amenazas != null && !planta.amenazas.isEmpty()) {
                    for (String plaga : planta.amenazas) {
                        TextView t = new TextView(PlantaDetalleActivity.this);
                        t.setText("• " + plaga);
                        t.setTextSize(16);
                        listPlagas.addView(t);
                    }
                } else {
                    TextView t = new TextView(PlantaDetalleActivity.this);
                    t.setText("Sin amenazas registradas");
                    t.setTextSize(14);
                    t.setAlpha(0.6f);
                    listPlagas.addView(t);
                }
            }
            @Override public void onCancelled(DatabaseError error) {}
        });
    }

    private void cargarImagenPorTipo(ImageView imgView, String tipo) {
        String nombreArchivo;
        switch (tipo != null ? tipo : "") {
            case "arbol":     nombreArchivo = "manzano.webp"; break;
            case "hierba":    nombreArchivo = "romero.webp";  break;
            case "flor":      nombreArchivo = "rosas.webp";   break;
            case "hortaliza": nombreArchivo = "tomate.webp";  break;
            case "fruta":     nombreArchivo = "sandias.webp"; break;
            default:
                imgView.setImageResource(R.drawable.ic_planta_placeholder);
                return;
        }
        Picasso.get()
                .load("file:///android_asset/" + nombreArchivo)
                .placeholder(R.drawable.ic_planta_placeholder)
                .error(R.drawable.ic_planta_placeholder)
                .into(imgView);
    }
}