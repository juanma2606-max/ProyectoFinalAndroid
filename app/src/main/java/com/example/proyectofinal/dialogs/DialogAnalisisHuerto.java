package com.example.proyectofinal.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.proyectofinal.R;
import com.example.proyectofinal.activities.ChatActivity;
import com.example.proyectofinal.dao.ChatDAO;

public class DialogAnalisisHuerto extends Dialog {

    private TextView txtTitulo;
    private ProgressBar progressBar;
    private TextView txtAnalisis;
    private Button btnCerrar;
    private Button btnContinuarChat;

    private final String prompt;
    private final ChatDAO chatDAO;
    private String analisisResultado = "";

    public DialogAnalisisHuerto(@NonNull Context context, String prompt) {
        super(context);
        this.prompt = prompt;
        this.chatDAO = new ChatDAO();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_analisis_huerto);

        // Configurar tamaño dialog (80% ancho, máximo 90% alto)
        if (getWindow() != null) {
            android.view.WindowManager.LayoutParams params = getWindow().getAttributes();
            params.width = (int) (getContext().getResources().getDisplayMetrics().widthPixels * 0.9);
            params.height = android.view.WindowManager.LayoutParams.WRAP_CONTENT;
            getWindow().setAttributes(params);
        }

        initViews();
        realizarAnalisis();

        btnCerrar.setOnClickListener(v -> dismiss());

        btnContinuarChat.setOnClickListener(v -> {
            Intent i = new Intent(getContext(), ChatActivity.class);
            i.putExtra("mensaje_inicial", analisisResultado);
            getContext().startActivity(i);
            dismiss();
        });
    }

    private void initViews() {
        txtTitulo = findViewById(R.id.txtTituloAnalisis);
        progressBar = findViewById(R.id.progressAnalisis);
        txtAnalisis = findViewById(R.id.txtAnalisis);
        btnCerrar = findViewById(R.id.btnCerrar);
        btnContinuarChat = findViewById(R.id.btnContinuarChat);

        // Estado inicial: cargando
        findViewById(R.id.layoutLoading).setVisibility(View.VISIBLE);
        findViewById(R.id.scrollAnalisis).setVisibility(View.GONE);
        btnContinuarChat.setEnabled(false);
    }

    private void realizarAnalisis() {
        chatDAO.analizarHuerto(prompt, new ChatDAO.OnChatResponseListener() {
            @Override
            public void onSuccess(String response) {
                analisisResultado = response;

                // Ocultar loading, mostrar resultado
                findViewById(R.id.layoutLoading).setVisibility(View.GONE);
                findViewById(R.id.scrollAnalisis).setVisibility(View.VISIBLE);
                txtAnalisis.setText(response);
                btnContinuarChat.setEnabled(true);
            }

            @Override
            public void onError(String message) {
                findViewById(R.id.layoutLoading).setVisibility(View.GONE);
                findViewById(R.id.scrollAnalisis).setVisibility(View.VISIBLE);
                txtAnalisis.setText("❌ Error al analizar: " + message + "\n\nIntenta de nuevo más tarde.");
                btnContinuarChat.setEnabled(false);
            }
        });
    }
}