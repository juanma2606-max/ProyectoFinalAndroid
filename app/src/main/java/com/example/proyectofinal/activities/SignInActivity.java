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
import com.example.proyectofinal.dao.PersonDAO;
import com.google.firebase.auth.FirebaseUser;

public class SignInActivity extends AppCompatActivity {

    private EditText usernameInput, emailInput, passwordInput, confirmPasswordInput;
    private Button registerButton;

    private Button loginButton;

    private PersonDAO personDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        personDAO = new PersonDAO();

        usernameInput       = findViewById(R.id.usernameInput);
        emailInput          = findViewById(R.id.emailInput);
        passwordInput       = findViewById(R.id.passwordInput);
        confirmPasswordInput= findViewById(R.id.confirmPasswordInput);
        registerButton      = findViewById(R.id.registerButton);
        loginButton         = findViewById(R.id.goToLoginButton);

        registerButton.setOnClickListener(v -> registerUser());
        loginButton.setOnClickListener(v ->
                startActivity(new Intent(SignInActivity.this, LoginActivity.class))
        );
    }

    private void registerUser() {
        String username = usernameInput.getText().toString().trim();
        String email    = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirm  = confirmPasswordInput.getText().toString().trim();

        // VALIDACIÓN USERNAME
        if (username.length() < 5) {
            usernameInput.setError("Mínimo 5 caracteres");
            return;
        }

        if (Validators.isRestrictedWord(username, new String[]{"admin", "superadmin"})) {
            usernameInput.setError("Nombre no permitido");
            return;
        }

        // VALIDACIÓN EMAIL
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Email inválido");
            return;
        }

        // VALIDACIÓN CONTRASEÑA FUERTE
        if (!Validators.isStrongPassword(password)) {
            passwordInput.setError("Debe contener mayúscula y número");
            return;
        }

        // VALIDACIÓN MATCH PASSWORD
        if (!Validators.passwordsMatch(password, confirm)) {
            confirmPasswordInput.setError("Las contraseñas no coinciden");
            return;
        }

        // REGISTRO — delegado completamente al DAO
        personDAO.registerWithEmail(email, password, username, new PersonDAO.OnRegisterCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                Toast.makeText(SignInActivity.this, "Cuenta creada correctamente", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(SignInActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}