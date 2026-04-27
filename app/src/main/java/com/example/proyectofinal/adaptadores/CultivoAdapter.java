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

    public interface OnCultivoActionListener {
        void onEditar(Cultivo cultivo);
        void onEliminar(Cultivo cultivo);
    }

    private final Context context;
    private List<Cultivo> lista;
    private final OnCultivoActionListener listener;
    private final PlantaDAO plantaDAO;

    public CultivoAdapter(Context context, List<Cultivo> lista, OnCultivoActionListener listener) {
        this.context = context;
        this.lista = lista;
        this.listener = listener;
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

        // Cantidad
        if (holder.txtCantidad != null) {
            holder.txtCantidad.setText(cultivo.getCantidadTexto());
        }

        // Estado con color y texto
        holder.txtEstado.setText(cultivo.getEstadoTexto());
        holder.txtEstado.setTextColor(cultivo.getEstadoColor());

        // Warning si enfermo
        if (holder.iconWarning != null) {
            if (cultivo.estaEnfermo()) {
                holder.iconWarning.setVisibility(View.VISIBLE);
            } else {
                holder.iconWarning.setVisibility(View.GONE);
            }
        }

        // Fecha siembra
        if (holder.txtFechaSiembra != null && cultivo.getFechaSiembra() != null) {
            String fecha = formatearFecha(cultivo.getFechaSiembra());
            holder.txtFechaSiembra.setText("Sembrado: " + fecha);
        }

        // Notas
        if (holder.txtNotas != null) {
            if (cultivo.tieneNotas()) {
                holder.txtNotas.setText(cultivo.getNotas());
                holder.txtNotas.setVisibility(View.VISIBLE);
            } else {
                holder.txtNotas.setVisibility(View.GONE);
            }
        }

        // Consulta secundaria a PlantaDAO para obtener datos de la planta
        plantaDAO.getPlantaById(cultivo.getPlantaId(), new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Planta planta = snapshot.getValue(Planta.class);
                if (planta == null) return;

                // Nombre planta
                holder.txtNombrePlanta.setText(planta.getNombre());

                // Cargar imagen de la planta
                if (planta.getImagen() != null && !planta.getImagen().isEmpty()) {
                    Picasso.get()
                            .load(planta.getImagen())
                            .placeholder(R.drawable.ic_launcher_background)
                            .error(R.drawable.ic_launcher_background)
                            .into(holder.imgCultivo);
                } else {
                    cargarImagenPorTipo(holder.imgCultivo, planta.getTipo());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                holder.txtNombrePlanta.setText("Planta desconocida");
            }
        });

        holder.btnEditar.setOnClickListener(v -> listener.onEditar(cultivo));
        holder.btnEliminar.setOnClickListener(v -> listener.onEliminar(cultivo));
    }

    private String formatearFecha(String fechaISO) {
        try {
            if (fechaISO != null && fechaISO.length() >= 10) {
                String fecha = fechaISO.substring(0, 10);
                String[] partes = fecha.split("-");
                if (partes.length == 3) {
                    return partes[2] + "/" + partes[1] + "/" + partes[0];
                }
            }
            return fechaISO;
        } catch (Exception e) {
            return fechaISO;
        }
    }

    @Override
    public int getItemCount() {
        return lista.size();
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

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgCultivo;
        ImageView iconWarning;
        TextView txtNombrePlanta;
        TextView txtCantidad;
        TextView txtEstado;
        TextView txtFechaSiembra;
        TextView txtNotas;
        Button btnEditar;
        Button btnEliminar;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCultivo = itemView.findViewById(R.id.imgCultivo);
            iconWarning = itemView.findViewById(R.id.iconWarning);
            txtNombrePlanta = itemView.findViewById(R.id.txtNombrePlanta);
            txtCantidad = itemView.findViewById(R.id.txtCantidadCultivo);
            txtEstado = itemView.findViewById(R.id.txtEstadoCultivo);
            txtFechaSiembra = itemView.findViewById(R.id.txtFechaSiembra);
            txtNotas = itemView.findViewById(R.id.txtNotasCultivo);
            btnEditar = itemView.findViewById(R.id.btnEditarCultivo);
            btnEliminar = itemView.findViewById(R.id.btnEliminarCultivo);
        }
    }
}