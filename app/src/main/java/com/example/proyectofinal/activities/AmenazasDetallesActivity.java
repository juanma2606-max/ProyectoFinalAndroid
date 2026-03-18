package com.example.proyectofinal.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.proyectofinal.R;
import com.example.proyectofinal.modelos.Amenaza;
import com.example.proyectofinal.dao.AmenazaDAO;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class AmenazasDetallesActivity extends AppCompatActivity {

    private ImageView imgAmenaza;
    private TextView txtNombre, txtTipoBadge, txtDescripcion, txtTratamiento;
    private LinearLayout listSintomas;

    private AmenazaDAO amenazaService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_amenaza_detalle);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Detalle de amenaza");
        }

        amenazaService = new AmenazaDAO();

        imgAmenaza       = findViewById(R.id.imgAmenazaDetalle);
        txtNombre        = findViewById(R.id.txtNombreAmenaza);
        txtTipoBadge     = findViewById(R.id.txtTipoBadge);
        txtDescripcion   = findViewById(R.id.txtDescripcionAmenaza);
        txtTratamiento   = findViewById(R.id.txtTratamiento);
        listSintomas     = findViewById(R.id.listSintomas);

        ImageButton btnVolver = findViewById(R.id.btnVolver);
        btnVolver.setOnClickListener(v -> finish());

        String id = getIntent().getStringExtra("id");
        cargarAmenaza(id);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void cargarAmenaza(String id) {
        amenazaService.getAmenazaById(id, new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Amenaza amenaza = snapshot.getValue(Amenaza.class);
                if (amenaza == null) return;

                txtNombre.setText(amenaza.nombre);
                txtDescripcion.setText(amenaza.descripcion);

                // Badge de tipo
                if (amenaza.tipo != null) {
                    txtTipoBadge.setText(amenaza.tipo.toUpperCase());
                    txtTipoBadge.setVisibility(android.view.View.VISIBLE);
                } else {
                    txtTipoBadge.setVisibility(android.view.View.GONE);
                }

                // Tratamiento
                if (amenaza.tratamiento != null && !amenaza.tratamiento.isEmpty()) {
                    txtTratamiento.setText(amenaza.tratamiento);
                    txtTratamiento.setVisibility(android.view.View.VISIBLE);
                    findViewById(R.id.lblTratamiento).setVisibility(android.view.View.VISIBLE);
                } else {
                    txtTratamiento.setVisibility(android.view.View.GONE);
                    findViewById(R.id.lblTratamiento).setVisibility(android.view.View.GONE);
                }

                // Síntomas
                listSintomas.removeAllViews();
                if (amenaza.sintomas != null && !amenaza.sintomas.isEmpty()) {
                    for (String s : amenaza.sintomas) {
                        TextView t = new TextView(AmenazasDetallesActivity.this);
                        t.setText("• " + s);
                        t.setTextSize(15);
                        t.setTextColor(0xFF333333);
                        listSintomas.addView(t);
                    }
                    listSintomas.setVisibility(android.view.View.VISIBLE);
                    findViewById(R.id.lblSintomas).setVisibility(android.view.View.VISIBLE);
                } else {
                    listSintomas.setVisibility(android.view.View.GONE);
                    findViewById(R.id.lblSintomas).setVisibility(android.view.View.GONE);
                }

                // Carga imagen según tipo de amenaza
                cargarImagenPorTipo(imgAmenaza, amenaza.tipo);
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }

    private void cargarImagenPorTipo(ImageView imgView, String tipo) {
        String nombreArchivo;
        switch (tipo != null ? tipo : "") {
            case "plaga":       nombreArchivo = "pulgon.webp"; break;
            case "enfermedad":  nombreArchivo = "mildiu.webp"; break;
            default:            nombreArchivo = "plaga_generico.webp"; break;
        }

        Picasso.get()
                .load("file:///android_asset/" + nombreArchivo)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .into(imgView);
    }
}