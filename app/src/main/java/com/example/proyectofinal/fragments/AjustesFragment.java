package com.example.proyectofinal.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

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
import com.squareup.picasso.Picasso;

public class AjustesFragment extends Fragment {

    private TextView txtNombre, txtEmail, txtProveedorAuth;
    private ImageView imgPerfil;
    private Button btnEditarNombre, btnCambiarFoto, btnCambiarPassword, btnLogout;

    private UserDAO userDAO;
    private AuthDAO authDAO;
    private FirebaseAuth auth;

    private User usuarioActual;
    private boolean tienePassword = false;

    private final String[] FOTOS_PERFIL = {
            "avatar2.webp",
            "avatar3.webp",
            "avatar4.webp",
            "avatar5.webp",
            "avatar6.webp"
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ajustes, container, false);

        userDAO = new UserDAO();
        authDAO = new AuthDAO();
        auth = FirebaseAuth.getInstance();

        txtNombre = view.findViewById(R.id.txtUsuarioNombre);
        txtEmail = view.findViewById(R.id.txtUsuarioEmail);
        txtProveedorAuth = view.findViewById(R.id.txtProveedorAuth);
        imgPerfil = view.findViewById(R.id.imgPerfil);

        btnEditarNombre = view.findViewById(R.id.btnEditarNombre);
        btnCambiarFoto = view.findViewById(R.id.btnCambiarFoto);
        btnCambiarPassword = view.findViewById(R.id.btnCambiarPassword);
        btnLogout = view.findViewById(R.id.btnLogout);

        verificarMetodoAuth();
        cargarUsuario();

        btnEditarNombre.setOnClickListener(v -> mostrarDialogoEditarNombre());
        btnCambiarFoto.setOnClickListener(v -> mostrarDialogoCambiarFoto());
        btnCambiarPassword.setOnClickListener(v -> mostrarDialogoCambiarPassword());
        btnLogout.setOnClickListener(v -> mostrarDialogoLogout());

        return view;
    }

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

    private void cargarUsuario() {
        String uid = auth.getUid();
        if (uid == null) return;

        userDAO.getPersonById(uid, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return;
                usuarioActual = snapshot.getValue(User.class);
                if (usuarioActual == null) return;

                if (usuarioActual.getUid() == null) {
                    usuarioActual.setUid(uid);
                }

                txtNombre.setText(usuarioActual.getUsername() != null ? usuarioActual.getUsername() : "Sin nombre");
                txtEmail.setText(usuarioActual.getEmail() != null ? usuarioActual.getEmail() : "Sin email");
                cargarFotoPerfil(usuarioActual.getFotoPerfil());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (isAdded()) Toast.makeText(getContext(), "Error al cargar usuario", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarFotoPerfil(String foto) {
        if (!isAdded() || getContext() == null || imgPerfil == null) return;

        if (foto == null || foto.isEmpty()) {
            imgPerfil.setImageResource(R.drawable.logo);
            return;
        }

        // Si es URL de Google, cargar con Picasso
        if (foto.startsWith("http")) {
            Picasso.get()
                    .load(foto)
                    .placeholder(R.drawable.logo)
                    .error(R.drawable.logo)
                    .into(imgPerfil);
            return;
        }

        // Si es avatar local
        String nombreSinExtension = foto.replace(".webp", "").replace(".png", "").replace(".jpg", "");
        int resId = getResources().getIdentifier(nombreSinExtension, "drawable", getContext().getPackageName());
        imgPerfil.setImageResource(resId != 0 ? resId : R.drawable.logo);
    }

    private void mostrarDialogoEditarNombre() {
        if (usuarioActual == null || !isAdded()) return;

        final EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        input.setPadding(40, 40, 40, 40);
        input.setText(usuarioActual.getUsername());

        new AlertDialog.Builder(requireContext())
                .setTitle("Editar nombre")
                .setView(input)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String nuevoNombre = input.getText().toString().trim();
                    if (!nuevoNombre.isEmpty()) guardarNombre(nuevoNombre);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void guardarNombre(String nuevoNombre) {
        String uid = auth.getUid();
        if (uid == null) return;

        userDAO.updateUsername(uid, nuevoNombre, new UserDAO.OnOperationCallback() {
            @Override
            public void onSuccess() {
                if (isAdded()) {
                    if (usuarioActual != null) usuarioActual.setUsername(nuevoNombre);
                    txtNombre.setText(nuevoNombre);
                    Toast.makeText(getContext(), "Nombre actualizado", Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onError(Exception e) {}
        });
    }

    private void mostrarDialogoCambiarFoto() {
        if (usuarioActual == null || !isAdded()) return;

        float density = getResources().getDisplayMetrics().density;
        int avatarSize = (int) (130 * density); // Larger square size
        int margin = (int) (12 * density);

        ScrollView scrollView = new ScrollView(requireContext());
        GridLayout gridLayout = new GridLayout(requireContext());
        gridLayout.setColumnCount(2); // 2 columns to show square photos
        gridLayout.setPadding(margin, margin, margin, margin);
        gridLayout.setAlignmentMode(GridLayout.ALIGN_BOUNDS);
        gridLayout.setUseDefaultMargins(false);

        final int[] seleccion = {-1};
        final CardView[] cards = new CardView[FOTOS_PERFIL.length];

        for (int i = 0; i < FOTOS_PERFIL.length; i++) {
            CardView card = new CardView(requireContext());
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = avatarSize;
            params.height = avatarSize;
            params.setMargins(margin, margin, margin, margin);
            params.setGravity(Gravity.CENTER);
            card.setLayoutParams(params);
            
            card.setRadius(20 * density); // Rounded corners
            card.setCardElevation(4 * density);
            card.setClipToOutline(true);

            ImageView img = new ImageView(requireContext());
            img.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, 
                    ViewGroup.LayoutParams.MATCH_PARENT));
            
            String name = FOTOS_PERFIL[i].replace(".webp", "");
            int resId = getResources().getIdentifier(name, "drawable", requireContext().getPackageName());
            img.setImageResource(resId != 0 ? resId : R.drawable.logo);
            img.setScaleType(ImageView.ScaleType.CENTER_CROP);

            card.addView(img);
            cards[i] = card;

            final int index = i;
            card.setOnClickListener(v -> {
                seleccion[0] = index;
                for (int j = 0; j < cards.length; j++) {
                    if (j == index) {
                        cards[j].setAlpha(1.0f);
                        cards[j].setCardElevation(15 * density);
                        cards[j].setCardBackgroundColor(0xFFE8F5E9); // Light green background
                    } else {
                        cards[j].setAlpha(0.4f);
                        cards[j].setCardElevation(2 * density);
                        cards[j].setCardBackgroundColor(Color.WHITE);
                    }
                }
            });
            gridLayout.addView(card);
        }

        // Center the gridLayout inside the scrollView
        LinearLayout centeringLayout = new LinearLayout(requireContext());
        centeringLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        centeringLayout.addView(gridLayout);

        scrollView.addView(centeringLayout);

        new AlertDialog.Builder(requireContext())
                .setTitle("Seleccionar avatar")
                .setView(scrollView)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    if (seleccion[0] != -1) {
                        guardarFoto(FOTOS_PERFIL[seleccion[0]]);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void guardarFoto(String foto) {
        String uid = auth.getUid();
        if (uid == null) {
            Toast.makeText(getContext(), "Error: sesión no válida", Toast.LENGTH_SHORT).show();
            return;
        }

        userDAO.updateProfilePhoto(uid, foto, new UserDAO.OnOperationCallback() {
            @Override
            public void onSuccess() {
                if (isAdded()) {
                    if (usuarioActual != null) usuarioActual.setFotoPerfil(foto);
                    cargarFotoPerfil(foto);
                    Toast.makeText(getContext(), "Foto actualizada", Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onError(Exception e) {
                if (isAdded()) Toast.makeText(getContext(), "Error al guardar", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarDialogoCambiarPassword() {
        if (!tienePassword || !isAdded()) return;
        
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_cambiar_password, null);
        TextInputEditText etActual = dialogView.findViewById(R.id.etPasswordActual);
        TextInputEditText etNueva = dialogView.findViewById(R.id.etPasswordNueva);
        TextInputEditText etConf = dialogView.findViewById(R.id.etPasswordConfirmar);

        new AlertDialog.Builder(requireContext())
                .setTitle("Cambiar contraseña")
                .setView(dialogView)
                .setPositiveButton("Cambiar", (dialog, which) -> {
                    String pActual = etActual.getText().toString();
                    String pNueva = etNueva.getText().toString();
                    String pConf = etConf.getText().toString();
                    
                    if (pNueva.equals(pConf) && pNueva.length() >= 6) {
                        cambiarPassword(pActual, pNueva);
                    } else {
                        Toast.makeText(getContext(), "Error en los datos", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void cambiarPassword(String actual, String nueva) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null || user.getEmail() == null) return;

        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), actual);
        user.reauthenticate(credential).addOnSuccessListener(aVoid -> {
            user.updatePassword(nueva).addOnSuccessListener(aVoid2 -> {
                if (isAdded()) Toast.makeText(getContext(), "Contraseña actualizada", Toast.LENGTH_SHORT).show();
            });
        }).addOnFailureListener(e -> {
            if (isAdded()) Toast.makeText(getContext(), "Error de autenticación", Toast.LENGTH_SHORT).show();
        });
    }

    private void mostrarDialogoLogout() {
        if (!isAdded()) return;
        new AlertDialog.Builder(requireContext())
                .setTitle("Cerrar sesión")
                .setMessage("¿Deseas salir?")
                .setPositiveButton("Sí", (d, w) -> {
                    authDAO.signOut();
                    startActivity(new Intent(requireActivity(), LoginActivity.class));
                    requireActivity().finish();
                })
                .setNegativeButton("No", null)
                .show();
    }
}
