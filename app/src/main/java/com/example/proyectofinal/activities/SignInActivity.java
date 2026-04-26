package com.example.proyectofinal.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.proyectofinal.R;
import com.example.proyectofinal.Validators.Validators;
import com.example.proyectofinal.dao.AuthDAO;
import com.google.firebase.auth.FirebaseUser;

public class SignInActivity extends AppCompatActivity {

    private EditText usernameInput, emailInput, passwordInput, confirmPasswordInput;
    private Button registerButton, loginButton;

    private AuthDAO authDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        authDAO = new AuthDAO();

        usernameInput = findViewById(R.id.usernameInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        registerButton = findViewById(R.id.registerButton);
        loginButton = findViewById(R.id.goToLoginButton);

        registerButton.setOnClickListener(v -> registerUser());
        loginButton.setOnClickListener(v ->
                startActivity(new Intent(SignInActivity.this, LoginActivity.class))
        );
    }

    private void registerUser() {
        String username = usernameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirm = confirmPasswordInput.getText().toString().trim();

        // VALIDACIÓN USERNAME
        if (username.length() < 5) {
            usernameInput.setError("Mínimo 5 caracteres");
            usernameInput.requestFocus();
            return;
        }

        if (Validators.isRestrictedWord(username, new String[]{"admin", "superadmin"})) {
            usernameInput.setError("Nombre no permitido");
            usernameInput.requestFocus();
            return;
        }

        // VALIDACIÓN EMAIL
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Email inválido");
            emailInput.requestFocus();
            return;
        }

        // VALIDACIÓN CONTRASEÑA FUERTE
        if (!Validators.isStrongPassword(password)) {
            passwordInput.setError("Debe contener mayúscula y número");
            passwordInput.requestFocus();
            return;
        }

        // VALIDACIÓN MATCH PASSWORD
        if (!Validators.passwordsMatch(password, confirm)) {
            confirmPasswordInput.setError("Las contraseñas no coinciden");
            confirmPasswordInput.requestFocus();
            return;
        }

        // REGISTRO — usa nuevo callback OnRegisterResult
        authDAO.register(email, password, username, new AuthDAO.OnRegisterResult() {
            @Override
            public void onSuccess(FirebaseUser user) {
                Toast.makeText(SignInActivity.this,
                        "Cuenta creada correctamente",
                        Toast.LENGTH_SHORT).show();

                // Ir a login o directamente a ContenedorActivity
                Intent intent = new Intent(SignInActivity.this, ContenedorActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(Exception e) {
                String mensaje = e.getMessage();

                // Mensajes más amigables
                if (mensaje != null) {
                    if (mensaje.contains("email address is already in use")) {
                        mensaje = "Este email ya está registrado";
                    } else if (mensaje.contains("network error")) {
                        mensaje = "Error de conexión. Verifica tu internet.";
                    } else if (mensaje.contains("Password should be at least 6 characters")) {
                        mensaje = "La contraseña debe tener al menos 6 caracteres";
                    }
                }

                Toast.makeText(SignInActivity.this,
                        "Error: " + mensaje,
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}