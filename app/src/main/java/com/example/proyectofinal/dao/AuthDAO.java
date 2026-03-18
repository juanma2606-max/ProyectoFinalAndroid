package com.example.proyectofinal.dao;

import com.example.proyectofinal.modelos.Person;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class AuthDAO {

    private final FirebaseAuth auth;
    private final DatabaseReference usersRef;

    public AuthDAO() {
        auth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("usuarios");
    }

    // ---------------------------------------------------------
    // INTERFAZ
    // ---------------------------------------------------------
    public interface OnAuthResult {
        void onSuccess(FirebaseUser user);
        void onError(Exception e);
    }

    // ---------------------------------------------------------
    // REGISTRO con email y contraseña
    // ---------------------------------------------------------
    public void register(String email, String password, OnAuthResult callback) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    FirebaseUser user = result.getUser();
                    if (user != null) callback.onSuccess(user);
                    else callback.onError(new Exception("Usuario nulo"));
                })
                .addOnFailureListener(callback::onError);
    }

    // ---------------------------------------------------------
    // LOGIN con email y contraseña
    // ---------------------------------------------------------
    public void login(String email, String password, OnAuthResult callback) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    FirebaseUser user = result.getUser();
                    if (user != null) callback.onSuccess(user);
                    else callback.onError(new Exception("Usuario nulo"));
                })
                .addOnFailureListener(callback::onError);
    }

    // ---------------------------------------------------------
    // LOGIN con Google (movido desde LoginActivity)
    // ---------------------------------------------------------
    public void loginWithGoogle(String idToken, boolean[] isNewUser, OnAuthResult callback) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        boolean newUser = task.getResult().getAdditionalUserInfo() != null
                                && task.getResult().getAdditionalUserInfo().isNewUser();
                        if (newUser) guardarUsuarioGoogle(user);
                        callback.onSuccess(user);
                    } else {
                        callback.onError(new Exception("Error autenticando con Google"));
                    }
                });
    }

    // ---------------------------------------------------------
    // Guardar usuario de Google en Realtime Database (movido desde LoginActivity)
    // ---------------------------------------------------------
    private void guardarUsuarioGoogle(FirebaseUser user) {
        if (user == null) return;

        Map<String, Object> datos = new HashMap<>();
        datos.put("nombre", user.getDisplayName());
        datos.put("email", user.getEmail());
        datos.put("foto", user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "");

        usersRef.child(user.getUid()).setValue(datos);
    }

    // ---------------------------------------------------------
    // Cerrar sesión
    // ---------------------------------------------------------
    public void signOut() {
        auth.signOut();
    }

    // ---------------------------------------------------------
    // Obtener usuario autenticado actual
    // ---------------------------------------------------------
    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }
}