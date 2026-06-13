package com.example.proyectofinal.activities;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyectofinal.R;
import com.example.proyectofinal.adaptadores.ChatAdapter;
import com.example.proyectofinal.dao.ChatDAO;
import com.example.proyectofinal.modelos.Mensaje;
import com.example.proyectofinal.utils.CloudinaryUploader;
import com.example.proyectofinal.utils.SnackbarHelper;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView rvMensajes;
    private EditText etMensaje;
    private Button btnEnviar;

    private ChatAdapter adapter;
    private List<Mensaje> mensajes;
    private ChatDAO chatDAO;

    private boolean enviando = false;
    private ImageButton btnAdjuntarImagen;
    private String imagenPendienteUrl = null;
    private ActivityResultLauncher<String> seleccionarImagenLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        initViews();
        setupRecyclerView();

        chatDAO = new ChatDAO();

        // Mensaje inicial de bienvenida
        mensajes.add(new Mensaje(
                "assistant",
                "¡Hola! Soy HuertingIA 🌱 Tu asistente experto en huertos y plantas. ¿En qué puedo ayudarte hoy?"
        ));
        adapter.notifyItemInserted(0);

        // Detectar si viene mensaje inicial desde análisis de huerto
        String mensajeInicial = getIntent().getStringExtra("mensaje_inicial");
        if (mensajeInicial != null && !mensajeInicial.isEmpty()) {
            // Agregar como mensaje del USUARIO para que IA responda
            mensajes.add(new Mensaje("user", "Tengo este análisis de mi huerto:\n\n" + mensajeInicial + "\n\n¿Qué más me recomiendas?"));
            adapter.notifyItemInserted(mensajes.size() - 1);
            scrollAlFinal();

            // Enviar automáticamente para que IA responda
            enviarMensajeInicial();
        }

        btnEnviar.setOnClickListener(v -> enviarMensaje());
        seleccionarImagenLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        subirImagenChat(uri);
                    }
                }
        );

        btnAdjuntarImagen = findViewById(R.id.btnAdjuntarImagen);
        btnAdjuntarImagen.setOnClickListener(v -> seleccionarImagenLauncher.launch("image/*"));
        findViewById(R.id.btnQuitarImagen).setOnClickListener(v -> {
            imagenPendienteUrl = null;
            findViewById(R.id.layoutPreviewImagen).setVisibility(View.GONE);
        });
    }

    private void subirImagenChat(Uri uri) {
        SnackbarHelper.show(this, "Subiendo imagen...");
        btnAdjuntarImagen.setEnabled(false);

        // Mostrar preview local inmediatamente
        ImageView imgPreview = findViewById(R.id.imgPreviewChat);
        imgPreview.setImageURI(uri);
        findViewById(R.id.layoutPreviewImagen).setVisibility(View.VISIBLE);

        CloudinaryUploader.subirImagen(this, uri, new CloudinaryUploader.OnUploadResult() {
            @Override
            public void onSuccess(String secureUrl) {
                imagenPendienteUrl = secureUrl;
                btnAdjuntarImagen.setEnabled(true);
                SnackbarHelper.showSuccess(ChatActivity.this, "Imagen lista ✓ escribe tu pregunta");
            }

            @Override
            public void onError(String mensaje) {
                imagenPendienteUrl = null;
                btnAdjuntarImagen.setEnabled(true);
                findViewById(R.id.layoutPreviewImagen).setVisibility(View.GONE);
                SnackbarHelper.showError(ChatActivity.this, "Error al subir imagen: " + mensaje);
            }
        });
    }

    /**
     * Enviar mensaje inicial del análisis para que IA responda
     */
    private void enviarMensajeInicial() {
        enviando = true;
        btnEnviar.setEnabled(false);
        btnEnviar.setText("...");

        chatDAO.enviarMensaje(mensajes, new ChatDAO.OnChatResponseListener() {
            @Override
            public void onSuccess(String response) {
                Mensaje mensajeIA = new Mensaje("assistant", response);
                mensajes.add(mensajeIA);
                adapter.notifyItemInserted(mensajes.size() - 1);
                scrollAlFinal();

                enviando = false;
                btnEnviar.setEnabled(true);
                btnEnviar.setText("▶");
            }

            @Override
            public void onError(String message) {
                Toast.makeText(ChatActivity.this,
                        "Error: " + message,
                        Toast.LENGTH_SHORT).show();

                enviando = false;
                btnEnviar.setEnabled(true);
                btnEnviar.setText("▶");
            }
        });
    }

    private void initViews() {
        rvMensajes = findViewById(R.id.rvMensajes);
        etMensaje = findViewById(R.id.etMensaje);
        btnEnviar = findViewById(R.id.btnEnviar);
    }

    private void setupRecyclerView() {
        mensajes = new ArrayList<>();
        adapter = new ChatAdapter(this, mensajes);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // Scroll automático al final

        rvMensajes.setLayoutManager(layoutManager);
        rvMensajes.setAdapter(adapter);
    }

    private void enviarMensaje() {
        String texto = etMensaje.getText().toString().trim();

        if (texto.isEmpty() && imagenPendienteUrl == null) {
            Toast.makeText(this, "Escribe un mensaje o adjunta una imagen", Toast.LENGTH_SHORT).show();
            return;
        }

        if (enviando) {
            Toast.makeText(this, "Espera la respuesta anterior", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear mensaje con o sin imagen
        Mensaje mensajeUser;
        if (imagenPendienteUrl != null) {
            mensajeUser = new Mensaje("user", texto.isEmpty() ? "Analiza esta imagen" : texto, imagenPendienteUrl);
        } else {
            mensajeUser = new Mensaje("user", texto);
        }

        mensajes.add(mensajeUser);
        adapter.notifyItemInserted(mensajes.size() - 1);
        scrollAlFinal();

        // Limpiar imagen pendiente y ocultar preview
        imagenPendienteUrl = null;
        findViewById(R.id.layoutPreviewImagen).setVisibility(View.GONE);

        etMensaje.setText("");
        enviando = true;
        btnEnviar.setEnabled(false);
        btnEnviar.setText("...");

        chatDAO.enviarMensaje(mensajes, new ChatDAO.OnChatResponseListener() {
            @Override
            public void onSuccess(String response) {
                Mensaje mensajeIA = new Mensaje("assistant", response);
                mensajes.add(mensajeIA);
                adapter.notifyItemInserted(mensajes.size() - 1);
                scrollAlFinal();
                enviando = false;
                btnEnviar.setEnabled(true);
                btnEnviar.setText("▶");
            }

            @Override
            public void onError(String message) {
                Toast.makeText(ChatActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                if (!mensajes.isEmpty() && mensajes.get(mensajes.size() - 1).esUsuario()) {
                    mensajes.remove(mensajes.size() - 1);
                    adapter.notifyItemRemoved(mensajes.size());
                }
                enviando = false;
                btnEnviar.setEnabled(true);
                btnEnviar.setText("▶");
            }
        });
    }

    private void scrollAlFinal() {
        if (mensajes.size() > 0) {
            rvMensajes.smoothScrollToPosition(mensajes.size() - 1);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}