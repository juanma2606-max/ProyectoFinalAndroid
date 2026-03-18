package com.example.proyectofinal.adaptadores;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyectofinal.R;
import com.example.proyectofinal.dao.PlantaDAO;
import com.example.proyectofinal.modelos.Cultivo;
import com.example.proyectofinal.modelos.Planta;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;


import java.util.List;

public class CultivoAdapter extends RecyclerView.Adapter<CultivoAdapter.ViewHolder> {

    // ---------------------------------------------------------
    // Interfaz de acciones
    // ---------------------------------------------------------
    public interface OnCultivoActionListener {
        void onVer(Cultivo cultivo);
        void onEliminar(Cultivo cultivo);
    }

    private final Context context;
    private List<Cultivo> lista;
    private final OnCultivoActionListener listener;
    private final PlantaDAO plantaDAO;

    public CultivoAdapter(Context context, List<Cultivo> lista, OnCultivoActionListener listener) {
        this.context   = context;
        this.lista     = lista;
        this.listener  = listener;
        this.plantaDAO = new PlantaDAO();
    }

    public void updateList(List<Cultivo> nuevaLista) {
        this.lista = nuevaLista;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_cultivo, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Cultivo cultivo = lista.get(position);

        // Estado con color
        setEstadoColor(holder.txtEstado, cultivo.estado);

        // Consulta secundaria a PlantaDAO para obtener los datos de la planta
        plantaDAO.getPlantaById(cultivo.plantaId, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Planta planta = snapshot.getValue(Planta.class);
                if (planta == null) return;

                holder.txtNombrePlanta.setText(planta.nombre);
                holder.txtRiego.setText(planta.riego != null ? planta.riego : "-");
                holder.txtAbono.setText(planta.abono != null ? planta.abono : "-");
                holder.txtTiempo.setText(planta.tiempoCrecimiento != null ? planta.tiempoCrecimiento : "-");

                // Imagen según tipo de planta
                cargarImagenPorTipo(holder.imgCultivo, planta.tipo);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        holder.btnVer.setOnClickListener(v      -> listener.onVer(cultivo));
        holder.btnEliminar.setOnClickListener(v -> listener.onEliminar(cultivo));
    }

    // ---------------------------------------------------------
    // Color del badge de estado
    // ---------------------------------------------------------
    private void setEstadoColor(TextView txt, String estado) {
        if (estado == null) return;
        switch (estado) {
            case "cosechado":
                txt.setText("✅ Cosechado");
                txt.setTextColor(0xFF2E7D32);
                break;
            case "perdido":
                txt.setText("❌ Perdido");
                txt.setTextColor(0xFFC62828);
                break;
            default:
                txt.setText("🌱 Creciendo");
                txt.setTextColor(0xFF388E3C);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    // ---------------------------------------------------------
    // Imagen según tipo de planta desde assets
    // ---------------------------------------------------------
    private void cargarImagenPorTipo(ImageView imgView, String tipo) {
        String nombreArchivo;
        switch (tipo != null ? tipo : "") {
            case "arbol":     nombreArchivo = "manzano.webp"; break;
            case "hierba":    nombreArchivo = "romero.webp";  break;
            case "flor":      nombreArchivo = "rosas.webp";   break;
            case "hortaliza": nombreArchivo = "tomates.webp"; break;
            case "fruta":     nombreArchivo = "sandias.webp"; break;
            default:          nombreArchivo = "tomates.webp"; break;
        }
        Picasso.get()
                .load("file:///android_asset/" + nombreArchivo)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .into(imgView);
    }

    // ---------------------------------------------------------
    // ViewHolder
    // ---------------------------------------------------------
    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgCultivo;
        TextView txtNombrePlanta, txtRiego, txtAbono, txtTiempo, txtEstado;
        Button btnVer, btnEliminar;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCultivo      = itemView.findViewById(R.id.imgCultivo);
            txtNombrePlanta = itemView.findViewById(R.id.txtNombrePlanta);
            txtRiego        = itemView.findViewById(R.id.txtRiegoCultivo);
            txtAbono        = itemView.findViewById(R.id.txtAbonoCultivo);
            txtTiempo       = itemView.findViewById(R.id.txtTiempoCultivo);
            txtEstado       = itemView.findViewById(R.id.txtEstadoCultivo);
            btnVer          = itemView.findViewById(R.id.btnVerCultivo);
            btnEliminar     = itemView.findViewById(R.id.btnEliminarCultivo);
        }
    }
}