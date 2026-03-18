package com.example.proyectofinal.adaptadores;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyectofinal.R;
import com.example.proyectofinal.modelos.Planta;
import com.squareup.picasso.Picasso;

import java.util.List;

public class PlantaAdapter extends RecyclerView.Adapter<PlantaAdapter.PlantaViewHolder> {

    private List<Planta> plantas;
    private final Context context;

    public interface OnPlantaClick {
        void onClick(Planta planta);
    }

    private final OnPlantaClick listener;

    public PlantaAdapter(Context context, List<Planta> plantas, OnPlantaClick listener) {
        this.context = context;
        this.plantas = plantas;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PlantaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_planta, parent, false);
        return new PlantaViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PlantaViewHolder holder, int position) {
        Planta p = plantas.get(position);
        holder.txtNombre.setText(p.nombre);

        cargarImagenPorTipo(holder.imgPlanta, p.tipo);

        holder.itemView.setOnClickListener(v -> listener.onClick(p));
    }

    @Override
    public int getItemCount() {
        return plantas.size();
    }

    public void updateList(List<Planta> nuevas) {
        this.plantas = nuevas;
        notifyDataSetChanged();
    }

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

    static class PlantaViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPlanta;
        TextView txtNombre;

        public PlantaViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPlanta = itemView.findViewById(R.id.imgPlanta);
            txtNombre = itemView.findViewById(R.id.txtNombre);
        }
    }
}