package com.example.proyectofinal.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.proyectofinal.R;
import com.example.proyectofinal.dao.AuthDAO;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText emailInput, passwordInput;
    private Button loginButton, goToRegisterButton, btnGoogle;

    private AuthDAO authDAO;
    private GoogleSignInClient googleSignInClient;
    private ActivityResultLauncher<Intent> googleLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authDAO = new AuthDAO();

        emailInput        = findViewById(R.id.emailInput);
        passwordInput     = findViewById(R.id.passwordInput);
        loginButton       = findViewById(R.id.loginButton);
        goToRegisterButton= findViewById(R.id.goToRegisterButton);
        btnGoogle         = findViewById(R.id.btnGoogle);

        // Configuración Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // Launcher para recibir resultado de Google
        googleLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Task<GoogleSignInAccount> task =
                                GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        try {
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            // Delegar completamente al DAO
                            authDAO.loginWithGoogle(account.getIdToken(), null, new AuthDAO.OnAuthResult() {
                                @Override
                                public void onSuccess(FirebaseUser user) {
                                    Toast.makeText(LoginActivity.this, "Login con Google correcto", Toast.LENGTH_SHORT).show();
                                    irAlContenedor();
                                }

                                @Override
                                public void onError(Exception e) {
                                    Toast.makeText(LoginActivity.this, "Error autenticando con Google", Toast.LENGTH_LONG).show();
                                }
                            });
                        } catch (ApiException e) {
                            Toast.makeText(this, "Error en login Google", Toast.LENGTH_LONG).show();
                        }
                    }
                }
        );

        loginButton.setOnClickListener(v -> loginUsuario());

        btnGoogle.setOnClickListener(v ->
                googleLauncher.launch(googleSignInClient.getSignInIntent())
        );

        goToRegisterButton.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, SignInActivity.class))
        );
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Forzar logout para evitar sesiones previas al abrir la pantalla
        googleSignInClient.signOut();
        authDAO.signOut();

        if (authDAO.getCurrentUser() != null) {
            irAlContenedor();
        }
    }

    // ---------------------------------------------------------
    // Login con email/contraseña — solo UI y delegación al DAO
    // ---------------------------------------------------------
    private void loginUsuario() {
        String email    = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        authDAO.login(email, password, new AuthDAO.OnAuthResult() {
            @Override
            public void onSuccess(FirebaseUser user) {
                Toast.makeText(LoginActivity.this, "Inicio de sesión correcto", Toast.LENGTH_SHORT).show();
                irAlContenedor();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(LoginActivity.this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ---------------------------------------------------------
    // Navegar al contenedor principal
    // ---------------------------------------------------------
    private void irAlContenedor() {
        startActivity(new Intent(LoginActivity.this, ContenedorActivity.class));
        finish();
    }
}