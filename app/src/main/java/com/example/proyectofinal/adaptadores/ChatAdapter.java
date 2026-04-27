package com.example.proyectofinal.adaptadores;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyectofinal.R;
import com.example.proyectofinal.modelos.Mensaje;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_USER = 1;
    private static final int VIEW_TYPE_ASSISTANT = 2;

    private final Context context;
    private final List<Mensaje> mensajes;

    public ChatAdapter(Context context, List<Mensaje> mensajes) {
        this.context = context;
        this.mensajes = mensajes;
    }

    @Override
    public int getItemViewType(int position) {
        Mensaje mensaje = mensajes.get(position);
        return mensaje.esUsuario() ? VIEW_TYPE_USER : VIEW_TYPE_ASSISTANT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_USER) {
            View v = LayoutInflater.from(context).inflate(R.layout.item_mensaje_user, parent, false);
            return new UserViewHolder(v);
        } else {
            View v = LayoutInflater.from(context).inflate(R.layout.item_mensaje_assistant, parent, false);
            return new AssistantViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Mensaje mensaje = mensajes.get(position);

        if (holder instanceof UserViewHolder) {
            ((UserViewHolder) holder).txtMensaje.setText(mensaje.getContent());
        } else if (holder instanceof AssistantViewHolder) {
            ((AssistantViewHolder) holder).txtMensaje.setText(mensaje.getContent());
        }
    }

    @Override
    public int getItemCount() {
        return mensajes.size();
    }

    public void agregarMensaje(Mensaje mensaje) {
        mensajes.add(mensaje);
        notifyItemInserted(mensajes.size() - 1);
    }

    // ViewHolder Usuario (derecha, verde)
    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView txtMensaje;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            txtMensaje = itemView.findViewById(R.id.txtMensajeUser);
        }
    }

    // ViewHolder Asistente (izquierda, gris)
    static class AssistantViewHolder extends RecyclerView.ViewHolder {
        TextView txtMensaje;

        AssistantViewHolder(@NonNull View itemView) {
            super(itemView);
            txtMensaje = itemView.findViewById(R.id.txtMensajeAssistant);
        }
    }
}