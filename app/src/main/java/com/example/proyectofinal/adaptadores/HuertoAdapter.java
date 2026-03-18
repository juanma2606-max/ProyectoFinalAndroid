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

import java.util.List;

public class HuertoAdapter extends RecyclerView.Adapter<HuertoAdapter.ViewHolder> {

    // ---------------------------------------------------------
    // Interfaz de acciones — equivalente a @Output() en Angular
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
        this.context  = context;
        this.lista    = lista;
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

        // Nombre — equivalente a {{ huerto?.nombre || 'Sin nombre' }}
        holder.txtNombre.setText(huerto.nombre != null ? huerto.nombre : "Sin nombre");

        // Descripción
        holder.txtDescripcion.setText(huerto.descripcion != null ? huerto.descripcion : "");

        // Badge de tipo — equivalente al condicional [src] según tipo
        boolean esParcela = "parcela".equals(huerto.tipo);
        holder.txtTipo.setText(esParcela ? "🌱 Parcela" : "🪴 Maceta");

        // Imagen según tipo — equivalente a [src] condicional del html
        holder.imgHuerto.setImageResource(esParcela
                ? R.drawable.img_parcela
                : R.drawable.img_maceta);

        // Botones — equivalente a (click)="onEdit()", (click)="onDelete()", routerLink ver
        holder.btnVer.setOnClickListener(v      -> listener.onVer(huerto));
        holder.btnEditar.setOnClickListener(v   -> listener.onEditar(huerto));
        holder.btnEliminar.setOnClickListener(v -> listener.onEliminar(huerto));
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    // ---------------------------------------------------------
    // ViewHolder
    // ---------------------------------------------------------
    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgHuerto;
        TextView txtNombre, txtDescripcion, txtTipo;
        Button btnVer, btnEditar, btnEliminar;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgHuerto      = itemView.findViewById(R.id.imgHuerto);
            txtNombre      = itemView.findViewById(R.id.txtNombreHuerto);
            txtDescripcion = itemView.findViewById(R.id.txtDescripcionHuerto);
            txtTipo        = itemView.findViewById(R.id.txtTipoHuerto);
            btnVer         = itemView.findViewById(R.id.btnVerHuerto);
            btnEditar      = itemView.findViewById(R.id.btnEditarHuerto);
            btnEliminar    = itemView.findViewById(R.id.btnEliminarHuerto);
        }
    }
}