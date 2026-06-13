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
import com.example.proyectofinal.modelos.Huerto;
import com.squareup.picasso.Picasso;

import java.util.List;

public class HuertoAdapter extends RecyclerView.Adapter<HuertoAdapter.ViewHolder> {

    // ---------------------------------------------------------
    // Interfaz de acciones
    // ---------------------------------------------------------
    public interface OnHuertoActionListener {
        void onVer(Huerto huerto);
        void onEditar(Huerto huerto);
        void onEliminar(Huerto huerto);
    }

    private final Context context;
    private List<Huerto> lista;
    private final OnHuertoActionListener listener;

    public HuertoAdapter(Context context, List<Huerto> lista, OnHuertoActionListener listener) {
        this.context = context;
        this.lista = lista;
        this.listener = listener;
    }

    public void updateList(List<Huerto> nuevaLista) {
        this.lista = nuevaLista;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_huerto, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Huerto huerto = lista.get(position);

        // Nombre
        holder.txtNombre.setText(huerto.getNombre() != null
                ? huerto.getNombre()
                : "Sin nombre");

        // Descripción
        if (huerto.getDescripcion() != null && !huerto.getDescripcion().isEmpty()) {
            holder.txtDescripcion.setText(huerto.getDescripcion());
            holder.txtDescripcion.setVisibility(View.VISIBLE);
        } else {
            holder.txtDescripcion.setVisibility(View.GONE);
        }

        // Ubicación
        if (holder.txtUbicacion != null) {
            if (huerto.getUbicacion() != null && !huerto.getUbicacion().isEmpty()) {
                holder.txtUbicacion.setText("📍 " + huerto.getUbicacion());
                holder.txtUbicacion.setVisibility(View.VISIBLE);
            } else {
                holder.txtUbicacion.setVisibility(View.GONE);
            }
        }

        // Superficie
        if (holder.txtSuperficie != null) {
            holder.txtSuperficie.setText("📏 " + huerto.getSuperficieTexto());
        }

        // Tipo suelo
        if (holder.txtTipoSuelo != null) {
            holder.txtTipoSuelo.setText("🌱 " + huerto.getTipoSueloCapitalizado());
        }

        // Horas sol
        if (holder.txtHorasSol != null) {
            holder.txtHorasSol.setText("☀️ " + huerto.getHorasSolTexto());
        }

        // Riego
        if (holder.txtRiego != null) {
            holder.txtRiego.setText("💧 " + huerto.getRiegoTexto());
        }

        // Imagen del huerto - drawable local o URL de Cloudinary
        if (huerto.tieneFoto()) {
            String foto = huerto.getFoto();

            if (foto.startsWith("http://") || foto.startsWith("https://")) {
                // URL de Cloudinary → cargar con Picasso
                Picasso.get()
                        .load(foto)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_gallery)
                        .into(holder.imgHuerto);
            } else {
                // Nombre de drawable local → quitar prefijo y extensión
                String nombreSinExtension = foto
                        .replace("/images/", "")
                        .replace(".jpg", "")
                        .replace(".webp", "")
                        .replace(".png", "");

                int resId = context.getResources().getIdentifier(
                        nombreSinExtension,
                        "drawable",
                        context.getPackageName()
                );

                if (resId != 0) {
                    holder.imgHuerto.setImageResource(resId);
                } else {
                    holder.imgHuerto.setImageResource(android.R.drawable.ic_menu_gallery);
                }
            }
        } else {
            // Sin foto - placeholder genérico
            holder.imgHuerto.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        // CARD CLICKEABLE = VER HUERTO
        holder.itemView.setOnClickListener(v -> listener.onVer(huerto));

        // FAB EDITAR
        if (holder.btnEditar != null) {
            holder.btnEditar.setOnClickListener(v -> listener.onEditar(huerto));
        }

        // FAB ELIMINAR
        if (holder.btnEliminar != null) {
            holder.btnEliminar.setOnClickListener(v -> listener.onEliminar(huerto));
        }
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    // ---------------------------------------------------------
    // ViewHolder
    // ---------------------------------------------------------
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgHuerto;
        TextView txtNombre;
        TextView txtDescripcion;
        TextView txtUbicacion;
        TextView txtSuperficie;
        TextView txtTipoSuelo;
        TextView txtHorasSol;
        TextView txtRiego;
        View btnEditar;  // FAB
        View btnEliminar;  // FAB

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgHuerto = itemView.findViewById(R.id.imgHuerto);
            txtNombre = itemView.findViewById(R.id.txtNombreHuerto);
            txtDescripcion = itemView.findViewById(R.id.txtDescripcionHuerto);
            txtUbicacion = itemView.findViewById(R.id.txtUbicacionHuerto);
            txtSuperficie = itemView.findViewById(R.id.txtSuperficieHuerto);
            txtTipoSuelo = itemView.findViewById(R.id.txtTipoSueloHuerto);
            txtHorasSol = itemView.findViewById(R.id.txtHorasSolHuerto);
            txtRiego = itemView.findViewById(R.id.txtRiegoHuerto);
            btnEditar = itemView.findViewById(R.id.btnEditarHuerto);
            btnEliminar = itemView.findViewById(R.id.btnEliminarHuerto);
        }
    }
}