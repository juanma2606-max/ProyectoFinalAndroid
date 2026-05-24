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
import com.example.proyectofinal.modelos.Amenaza;
import com.example.proyectofinal.utils.IconosHelper;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AmenazaAdapter extends RecyclerView.Adapter<AmenazaAdapter.AmenazaViewHolder> {

    private List<Amenaza> amenazas;
    private final Context context;

    public interface OnAmenazaClick {
        void onClick(Amenaza amenaza);
    }

    private final OnAmenazaClick listener;

    public AmenazaAdapter(Context context, List<Amenaza> amenazas, OnAmenazaClick listener) {
        this.context = context;
        this.amenazas = amenazas;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AmenazaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_amenaza, parent, false);
        return new AmenazaViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AmenazaViewHolder holder, int position) {
        Amenaza a = amenazas.get(position);
        holder.txtNombre.setText(a.nombre);
        holder.txtTipo.setText("plaga".equals(a.tipo) ? "🐛 Plaga" : "🦠 Enfermedad");

        //Cargar imagen con fallback
        cargarImagen(holder.imgAmenaza, a.imagen, a.tipo);

        holder.itemView.setOnClickListener(v -> listener.onClick(a));
    }

    @Override
    public int getItemCount() {
        return amenazas.size();
    }

    public void updateList(List<Amenaza> nuevas) {
        this.amenazas = nuevas;
        notifyDataSetChanged();
    }

    /**
     * Cargar imagen desde URL o mostrar icono fallback
     */
    private void cargarImagen(ImageView imgView, String urlImagen, String tipo) {
        // Si no tiene URL, mostrar icono inmediatamente
        if (urlImagen == null || urlImagen.isEmpty()) {
            mostrarIconoFallback(imgView, tipo);
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
                        // Error al cargar - mostrar icono fallback
                        mostrarIconoFallback(imgView, tipo);
                    }
                });
    }

    /**
     * Mostrar icono fallback según tipo
     */
    private void mostrarIconoFallback(ImageView imgView, String tipo) {
        imgView.setImageResource(IconosHelper.getIconoAmenaza(tipo));
        imgView.setBackgroundColor(IconosHelper.getColorAmenaza(tipo));
        imgView.setPadding(16, 16, 16, 16);
    }

    static class AmenazaViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAmenaza;
        TextView txtNombre, txtTipo;

        public AmenazaViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAmenaza = itemView.findViewById(R.id.imgAmenaza);
            txtNombre  = itemView.findViewById(R.id.txtNombreAmenaza);
            txtTipo    = itemView.findViewById(R.id.txtTipoAmenaza);
        }
    }
}