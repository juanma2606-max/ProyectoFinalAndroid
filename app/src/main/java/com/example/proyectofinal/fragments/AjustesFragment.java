package com.example.proyectofinal.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.example.proyectofinal.MusicService;
import com.example.proyectofinal.R;
import com.example.proyectofinal.activities.LoginActivity;
import com.example.proyectofinal.dao.AuthDAO;
import com.example.proyectofinal.dao.UserDAO;
import com.example.proyectofinal.modelos.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class AjustesFragment extends Fragment {

    private SwitchCompat switchMusic;
    private TextView txtNombre, txtEmail;
    private SharedPreferences prefs;
    private static final String PREFS_NAME = "app_settings";

    private UserDAO personDAO;
    private AuthDAO authDAO;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ajustes, container, false);

        prefs     = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        personDAO = new UserDAO();
        authDAO   = new AuthDAO();

        txtNombre   = view.findViewById(R.id.txtUsuarioNombre);
        txtEmail    = view.findViewById(R.id.txtUsuarioEmail);
        switchMusic = view.findViewById(R.id.switch_music);
        Button btnLogout = view.findViewById(R.id.btnLogout);

        // Cargar estado guardado del switch
        switchMusic.setChecked(prefs.getBoolean("music_enabled", false));

        // Listener música de fondo
        switchMusic.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("music_enabled", isChecked).apply();
            if (isChecked) {
                Toast.makeText(getContext(), "Música activada", Toast.LENGTH_SHORT).show();
            } else {
                Intent musicIntent = new Intent(requireActivity(), MusicService.class);
                requireActivity().stopService(musicIntent);
                Toast.makeText(getContext(), "Música desactivada", Toast.LENGTH_SHORT).show();
            }
        });

        // Cargar datos del usuario
        cargarUsuario();

        // Logout con confirmación
        btnLogout.setOnClickListener(v -> mostrarDialogoLogout());

        return view;
    }

    // ---------------------------------------------------------
    // Carga nombre y email del usuario desde Firebase
    // ---------------------------------------------------------
    private void cargarUsuario() {
        String uid = personDAO.getUserAuth() != null ? personDAO.getUserAuth().getUid() : null;
        if (uid == null) return;

        personDAO.getPersonById(uid, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User person = snapshot.getValue(User.class);
                if (person == null) return;
                txtNombre.setText(person.username != null ? person.username : "Sin nombre");
                txtEmail.setText(person.email != null ? person.email : "Sin email");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error al cargar usuario", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ---------------------------------------------------------
    // Diálogo de confirmación antes de cerrar sesión
    // ---------------------------------------------------------
    private void mostrarDialogoLogout() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Cerrar sesión")
                .setMessage("¿Seguro que quieres cerrar sesión?")
                .setPositiveButton("Cerrar sesión", (d, w) -> {
                    authDAO.signOut();
                    Intent intent = new Intent(requireActivity(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}