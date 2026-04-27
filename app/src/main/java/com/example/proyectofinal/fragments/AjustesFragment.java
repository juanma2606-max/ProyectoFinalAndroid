package com.example.proyectofinal.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.proyectofinal.MusicService;
import com.example.proyectofinal.R;
import com.example.proyectofinal.activities.LoginActivity;
import com.example.proyectofinal.dao.AuthDAO;
import com.example.proyectofinal.dao.UserDAO;
import com.example.proyectofinal.modelos.User;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class AjustesFragment extends Fragment {

    private SwitchCompat switchMusic;
    private TextView txtNombre, txtEmail, txtProveedorAuth;
    private ImageView imgPerfil;
    private Button btnEditarNombre, btnCambiarFoto, btnCambiarPassword, btnLogout;

    private SharedPreferences prefs;
    private static final String PREFS_NAME = "app_settings";

    private UserDAO userDAO;
    private AuthDAO authDAO;
    private FirebaseAuth auth;

    private User usuarioActual;
    private boolean tienePassword = false;

    // Fotos de perfil disponibles (mismo que web)
    private final String[] FOTOS_PERFIL = {
            "perfil1.png",
            "perfil2.png",
            "perfil3.png",
            "perfil4.png",
            "perfil5.png"
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ajustes, container, false);

        prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        userDAO = new UserDAO();
        authDAO = new AuthDAO();
        auth = FirebaseAuth.getInstance();

        // Inicializar vistas
        txtNombre = view.findViewById(R.id.txtUsuarioNombre);
        txtEmail = view.findViewById(R.id.txtUsuarioEmail);
        txtProveedorAuth = view.findViewById(R.id.txtProveedorAuth);
        imgPerfil = view.findViewById(R.id.imgPerfil);

        btnEditarNombre = view.findViewById(R.id.btnEditarNombre);
        btnCambiarFoto = view.findViewById(R.id.btnCambiarFoto);
        btnCambiarPassword = view.findViewById(R.id.btnCambiarPassword);
        btnLogout = view.findViewById(R.id.btnLogout);

        switchMusic = view.findViewById(R.id.switch_music);

        // Cargar estado música
        switchMusic.setChecked(prefs.getBoolean("music_enabled", false));
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

        // Verificar método de autenticación
        verificarMetodoAuth();

        // Cargar datos del usuario
        cargarUsuario();

        // Listeners
        btnEditarNombre.setOnClickListener(v -> mostrarDialogoEditarNombre());
        btnCambiarFoto.setOnClickListener(v -> mostrarDialogoCambiarFoto());
        btnCambiarPassword.setOnClickListener(v -> mostrarDialogoCambiarPassword());
        btnLogout.setOnClickListener(v -> mostrarDialogoLogout());

        return view;
    }

    // ---------------------------------------------------------
    // Verificar método de autenticación (Google vs Email)
    // ---------------------------------------------------------
    private void verificarMetodoAuth() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        for (UserInfo profile : user.getProviderData()) {
            String providerId = profile.getProviderId();

            if (providerId.equals("password")) {
                tienePassword = true;
                txtProveedorAuth.setText("Autenticación: Email/Contraseña");
                btnCambiarPassword.setVisibility(View.VISIBLE);
            } else if (providerId.equals("google.com")) {
                tienePassword = false;
                txtProveedorAuth.setText("Autenticación: Google");
                btnCambiarPassword.setVisibility(View.GONE);
            }
        }
    }

    // ---------------------------------------------------------
    // Cargar datos del usuario
    // ---------------------------------------------------------
    private void cargarUsuario() {
        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (uid == null) return;

        userDAO.getPersonById(uid, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usuarioActual = snapshot.getValue(User.class);
                if (usuarioActual == null) return;

                txtNombre.setText(usuarioActual.getUsername() != null
                        ? usuarioActual.getUsername()
                        : "Sin nombre");

                txtEmail.setText(usuarioActual.getEmail() != null
                        ? usuarioActual.getEmail()
                        : "Sin email");

                // Cargar foto perfil (desde drawable)
                cargarFotoPerfil(usuarioActual.getFotoPerfil());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error al cargar usuario", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ---------------------------------------------------------
    // Cargar foto de perfil
    // ---------------------------------------------------------
    private void cargarFotoPerfil(String foto) {
        if (foto == null || foto.isEmpty()) {
            foto = FOTOS_PERFIL[0];
        }

        String nombreSinExtension = foto.replace(".png", "").replace(".jpg", "");
        int resId = getResources().getIdentifier(
                nombreSinExtension,
                "drawable",
                requireContext().getPackageName()
        );

        if (resId != 0) {
            imgPerfil.setImageResource(resId);
        } else {
            imgPerfil.setImageResource(android.R.drawable.ic_menu_myplaces);
        }
    }

    // ---------------------------------------------------------
    // EDITAR NOMBRE
    // ---------------------------------------------------------
    private void mostrarDialogoEditarNombre() {
        if (usuarioActual == null) return;

        final EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        input.setText(usuarioActual.getUsername());
        input.setHint("Nuevo nombre");

        new AlertDialog.Builder(requireContext())
                .setTitle("Editar nombre de usuario")
                .setView(input)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String nuevoNombre = input.getText().toString().trim();

                    if (nuevoNombre.isEmpty()) {
                        Toast.makeText(getContext(), "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (nuevoNombre.length() < 3) {
                        Toast.makeText(getContext(), "El nombre debe tener al menos 3 caracteres", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    guardarNombre(nuevoNombre);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void guardarNombre(String nuevoNombre) {
        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (uid == null) {
            Toast.makeText(getContext(), "Error: usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        userDAO.updateUsername(uid, nuevoNombre, new UserDAO.OnOperationCallback() {
            @Override
            public void onSuccess() {
                usuarioActual.setUsername(nuevoNombre);
                txtNombre.setText(nuevoNombre);
                Toast.makeText(getContext(), "Nombre actualizado", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(getContext(), "Error al actualizar nombre", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ---------------------------------------------------------
    // CAMBIAR FOTO
    // ---------------------------------------------------------
    private void mostrarDialogoCambiarFoto() {
        if (usuarioActual == null) return;

        // Selector simple con nombres
        String[] nombresFotos = new String[FOTOS_PERFIL.length];  // ← CORREGIDO
        for (int i = 0; i < FOTOS_PERFIL.length; i++) {
            nombresFotos[i] = "Foto " + (i + 1);
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Seleccionar foto de perfil")
                .setItems(nombresFotos, (dialog, which) -> {
                    String fotoSeleccionada = FOTOS_PERFIL[which];
                    guardarFoto(fotoSeleccionada);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void guardarFoto(String foto) {
        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (uid == null) return;
        userDAO.updateProfilePhoto(usuarioActual.getUid(), foto, new UserDAO.OnOperationCallback() {
            @Override
            public void onSuccess() {
                usuarioActual.setFotoPerfil(foto);
                cargarFotoPerfil(foto);
                Toast.makeText(getContext(), "Foto actualizada", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(getContext(), "Error al actualizar foto", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ---------------------------------------------------------
    // CAMBIAR CONTRASEÑA (solo si tienePassword = true)
    // ---------------------------------------------------------
    private void mostrarDialogoCambiarPassword() {
        if (!tienePassword) {
            Toast.makeText(getContext(), "No puedes cambiar contraseña con cuenta de Google", Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_cambiar_password, null);

        TextInputEditText etPasswordActual = dialogView.findViewById(R.id.etPasswordActual);
        TextInputEditText etPasswordNueva = dialogView.findViewById(R.id.etPasswordNueva);
        TextInputEditText etPasswordConfirmar = dialogView.findViewById(R.id.etPasswordConfirmar);

        new AlertDialog.Builder(requireContext())
                .setTitle("Cambiar contraseña")
                .setView(dialogView)
                .setPositiveButton("Cambiar", (dialog, which) -> {
                    String actual = etPasswordActual.getText().toString();
                    String nueva = etPasswordNueva.getText().toString();
                    String confirmar = etPasswordConfirmar.getText().toString();

                    cambiarPassword(actual, nueva, confirmar);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void cambiarPassword(String actual, String nueva, String confirmar) {
        // Validaciones
        if (actual.isEmpty() || nueva.isEmpty() || confirmar.isEmpty()) {
            Toast.makeText(getContext(), "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        if (nueva.length() < 6) {
            Toast.makeText(getContext(), "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!nueva.equals(confirmar)) {
            Toast.makeText(getContext(), "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            return;
        }

        // Reautenticar y cambiar contraseña
        FirebaseUser user = auth.getCurrentUser();
        if (user == null || user.getEmail() == null) return;

        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), actual);

        user.reauthenticate(credential)
                .addOnSuccessListener(aVoid -> {
                    user.updatePassword(nueva)
                            .addOnSuccessListener(aVoid2 -> {
                                Toast.makeText(getContext(), "Contraseña actualizada ✅", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Error al actualizar contraseña", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Contraseña actual incorrecta", Toast.LENGTH_SHORT).show();
                });
    }

    // ---------------------------------------------------------
    // CERRAR SESIÓN
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