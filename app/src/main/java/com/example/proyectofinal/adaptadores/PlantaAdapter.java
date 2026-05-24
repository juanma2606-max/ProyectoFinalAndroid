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
import com.example.proyectofinal.utils.IconosHelper;
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

        // Nombre
        holder.txtNombre.setText(p.getNombre());

        // Nombre científico (si existe en layout)
        if (holder.txtNombreCientifico != null) {
            if (p.getNombreCientifico() != null && !p.getNombreCientifico().isEmpty()) {
                holder.txtNombreCientifico.setText(p.getNombreCientifico());
                holder.txtNombreCientifico.setVisibility(View.VISIBLE);
            } else {
                holder.txtNombreCientifico.setVisibility(View.GONE);
            }
        }

        // Tipo con capitalizado
        if (holder.txtTipo != null) {
            holder.txtTipo.setText(p.getTipoCapitalizado());
        }

        // Tiempo crecimiento
        if (holder.txtTiempo != null) {
            holder.txtTiempo.setText(p.getTiempoCrecimientoTexto());
        }

        // Riego
        if (holder.txtRiego != null) {
            holder.txtRiego.setText("💧 " + (p.getRiego() != null ? p.getRiego() : "-"));
        }

        // Luz
        if (holder.txtLuz != null) {
            String emojiLuz = getEmojiLuz(p.getLuz());
            holder.txtLuz.setText(emojiLuz + " " + (p.getLuz() != null ? p.getLuz() : "-"));
        }

        //Cargar imagen con fallback
        cargarImagen(holder.imgPlanta, p.getImagen(), p.getTipo());

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

    // ---------------------------------------------------------
    // Emoji según nivel de luz
    // ---------------------------------------------------------
    private String getEmojiLuz(String luz) {
        if (luz == null) return "☀️";
        switch (luz.toLowerCase()) {
            case "sombra":
                return "🌑";
            case "semi-sombra":
                return "⛅";
            case "pleno-sol":
                return "☀️";
            default:
                return "☀️";
        }
    }

    /**
     * Cargar imagen desde URL o mostrar color fallback
     */
    private void cargarImagen(ImageView imgView, String urlImagen, String tipo) {
        // Si no tiene URL, mostrar color inmediatamente
        if (urlImagen == null || urlImagen.isEmpty()) {
            mostrarColorFallback(imgView, tipo);
            return;
        }

        // Intentar cargar la imagen
        Picasso.get()
                .load(urlImagen)
                .placeholder(R.drawable.ic_launcher_background)
                .into(imgView, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
                        // Imagen cargada - limpiar el fondo de color
                        imgView.setBackgroundColor(0x00000000); // Transparente
                        imgView.setPadding(0, 0, 0, 0);
                    }

                    @Override
                    public void onError(Exception e) {
                        // Error al cargar - mostrar color fallback
                        mostrarColorFallback(imgView, tipo);
                    }
                });
    }

    /**
     * Mostrar fondo de color sin icono
     */
    private void mostrarColorFallback(ImageView imgView, String tipo) {
        // Limpiar la imagen (sin icono)
        imgView.setImageDrawable(null);

        // Solo aplicar el color de fondo
        imgView.setBackgroundColor(IconosHelper.getColorPlanta(tipo));
        imgView.setBackgroundColor(IconosHelper.getColorPlanta(tipo));

        // Sin padding
        imgView.setPadding(0, 0, 0, 0);
    }

    // ---------------------------------------------------------
    // ViewHolder
    // ---------------------------------------------------------
    static class PlantaViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPlanta;
        TextView txtNombre;
        TextView txtNombreCientifico;
        TextView txtTipo;
        TextView txtTiempo;
        TextView txtRiego;
        TextView txtLuz;

        public PlantaViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPlanta = itemView.findViewById(R.id.imgPlanta);
            txtNombre = itemView.findViewById(R.id.txtNombre);
            txtNombreCientifico = itemView.findViewById(R.id.txtNombreCientifico);
            txtTipo = itemView.findViewById(R.id.txtTipoPlanta);
            txtTiempo = itemView.findViewById(R.id.txtTiempoPlanta);
            txtRiego = itemView.findViewById(R.id.txtRiegoPlanta);
            txtLuz = itemView.findViewById(R.id.txtLuzPlanta);
        }
    }
}