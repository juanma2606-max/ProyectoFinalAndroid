package com.example.proyectofinal.adaptadores;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyectofinal.R;
import com.example.proyectofinal.modelos.Planta;
import com.squareup.picasso.Picasso;

import java.util.List;

public class PlantaSelectorAdapter extends RecyclerView.Adapter<PlantaSelectorAdapter.ViewHolder> {

    public interface OnPlantaSeleccionadaListener {
        void onPlantaSeleccionada(Planta planta);
    }

    private final Context context;
    private final List<Planta> plantas;
    private final OnPlantaSeleccionadaListener listener;
    private Planta plantaSeleccionada = null;

    public PlantaSelectorAdapter(Context context, List<Planta> plantas, OnPlantaSeleccionadaListener listener) {
        this.context = context;
        this.plantas = plantas;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_planta_selector, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Planta planta = plantas.get(position);

        holder.txtNombre.setText(planta.getNombre());

        // Imagen
        if (planta.getImagen() != null && !planta.getImagen().isEmpty()) {
            Picasso.get()
                    .load(planta.getImagen())
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .into(holder.imgPlanta);
        } else {
            cargarImagenPorTipo(holder.imgPlanta, planta.getTipo());
        }

        // Marcar si está seleccionada
        boolean esSeleccionada = plantaSeleccionada != null &&
                plantaSeleccionada.getId().equals(planta.getId());

        if (esSeleccionada) {
            holder.card.setCardBackgroundColor(0xFFE8F5E9); // Verde claro
            holder.card.setCardElevation(8); // Más elevación
        } else {
            holder.card.setCardBackgroundColor(0xFFFFFFFF); // Blanco
            holder.card.setCardElevation(2); // Elevación normal
        }

        // Click
        holder.itemView.setOnClickListener(v -> {
            plantaSeleccionada = planta;
            listener.onPlantaSeleccionada(planta);
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return plantas.size();
    }

    private void cargarImagenPorTipo(ImageView imgView, String tipo) {
        String nombreArchivo;
        switch (tipo != null ? tipo : "") {
            case "arbol":
                nombreArchivo = "manzano.webp";
                break;
            case "hierba":
                nombreArchivo = "romero.webp";
                break;
            case "flor":
                nombreArchivo = "rosas.webp";
                break;
            case "hortaliza":
                nombreArchivo = "tomates.webp";
                break;
            case "fruta":
                nombreArchivo = "sandias.webp";
                break;
            default:
                nombreArchivo = "tomates.webp";
                break;
        }

        Picasso.get()
                .load("file:///android_asset/" + nombreArchivo)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .into(imgView);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CardView card;
        ImageView imgPlanta;
        TextView txtNombre;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            card = (CardView) itemView;
            imgPlanta = itemView.findViewById(R.id.imgPlantaSelector);
            txtNombre = itemView.findViewById(R.id.txtNombrePlantaSelector);
        }
    }
}