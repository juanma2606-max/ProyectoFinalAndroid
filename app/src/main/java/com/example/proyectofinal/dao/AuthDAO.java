package com.example.proyectofinal.dao;

import com.example.proyectofinal.modelos.User;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

public class AuthDAO {

    private final FirebaseAuth auth;
    private final DatabaseReference usersRef;

    public AuthDAO() {
        auth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");
    }

    // ---------------------------------------------------------
    // Referencia al perfil del usuario
    // ---------------------------------------------------------
    private DatabaseReference getProfileRef(String uid) {
        return usersRef.child(uid).child("profile");
    }

    // ---------------------------------------------------------
    // INTERFACES
    // ---------------------------------------------------------
    public interface OnAuthResult {
        void onSuccess(FirebaseUser user);
        void onError(Exception e);
    }

    public interface OnLoginResult {
        void onSuccess(FirebaseUser user);
        void onBanned(String motivo);
        void onError(Exception e);
    }

    public interface OnRegisterResult {
        void onSuccess(FirebaseUser user);
        void onError(Exception e);
    }

    // ---------------------------------------------------------
    // REGISTRO con email y contraseña
    // ---------------------------------------------------------
    public void register(String email, String password, String username, OnRegisterResult callback) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    FirebaseUser firebaseUser = result.getUser();
                    if (firebaseUser != null) {
                        // Crear usuario en database en /users/{uid}/profile
                        User user = new User(firebaseUser.getUid(), username, email);
                        getProfileRef(firebaseUser.getUid()).setValue(user)
                                .addOnSuccessListener(aVoid -> callback.onSuccess(firebaseUser))
                                .addOnFailureListener(callback::onError);
                    } else {
                        callback.onError(new Exception("Usuario nulo tras registro"));
                    }
                })
                .addOnFailureListener(callback::onError);
    }

    // ---------------------------------------------------------
    // LOGIN con email y contraseña (con verificación de baneo)
    // ---------------------------------------------------------
    public void login(String email, String password, OnLoginResult callback) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    FirebaseUser firebaseUser = result.getUser();
                    if (firebaseUser == null) {
                        callback.onError(new Exception("Usuario nulo tras login"));
                        return;
                    }

                    // Verificar si está baneado
                    checkIfBanned(firebaseUser.getUid(), new OnBanCheckCallback() {
                        @Override
                        public void onBanned(boolean isBanned, String motivo) {
                            if (isBanned) {
                                // Cerrar sesión inmediatamente
                                auth.signOut();
                                callback.onBanned(motivo != null ? motivo : "Cuenta suspendida");
                            } else {
                                callback.onSuccess(firebaseUser);
                            }
                        }

                        @Override
                        public void onError(Exception e) {
                            // Si hay error verificando baneo, permitir login
                            // (para evitar bloquear usuarios por error de red)
                            callback.onSuccess(firebaseUser);
                        }
                    });
                })
                .addOnFailureListener(callback::onError);
    }

    // ---------------------------------------------------------
    // LOGIN con Google (con verificación de baneo)
    // ---------------------------------------------------------
    public void loginWithGoogle(String idToken, OnLoginResult callback) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        if (firebaseUser == null) {
                            callback.onError(new Exception("Usuario nulo tras login Google"));
                            return;
                        }

                        // No nos fiamos de isNewUser: comprobamos directamente si ya
                        // existe el nodo "profile" en la base de datos. Esto cubre el
                        // caso de cuentas de Auth que quedaron sin profile creado.
                        comprobarYCrearPerfilGoogle(firebaseUser, callback);
                    } else {
                        callback.onError(task.getException() != null
                                ? task.getException()
                                : new Exception("Error autenticando con Google"));
                    }
                });
    }

    // ---------------------------------------------------------
    // Comprueba si existe el profile del usuario de Google; si no
    // existe lo crea, y si existe verifica el baneo.
    // ---------------------------------------------------------
    private void comprobarYCrearPerfilGoogle(FirebaseUser firebaseUser, OnLoginResult callback) {
        getProfileRef(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    // No existe el profile -> lo creamos ahora
                    android.util.Log.d("GOOGLE_LOGIN", "No existe profile para "
                            + firebaseUser.getUid() + ", creando...");
                    guardarUsuarioGoogle(firebaseUser, new OnRegisterResult() {
                        @Override
                        public void onSuccess(FirebaseUser user) {
                            callback.onSuccess(user);
                        }

                        @Override
                        public void onError(Exception e) {
                            callback.onError(e);
                        }
                    });
                } else {
                    User user = snapshot.getValue(User.class);
                    if (user != null && user.estaBaneado()) {
                        auth.signOut();
                        callback.onBanned(user.getMotivoBaneo() != null
                                ? user.getMotivoBaneo() : "Cuenta suspendida");
                    } else {
                        callback.onSuccess(firebaseUser);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.toException());
            }
        });
    }

    // ---------------------------------------------------------
    // Verificar si usuario está baneado
    // ---------------------------------------------------------
    private void checkIfBanned(String uid, OnBanCheckCallback callback) {
        getProfileRef(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null && user.estaBaneado()) {
                    callback.onBanned(true, user.getMotivoBaneo());
                } else {
                    callback.onBanned(false, null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.toException());
            }
        });
    }

    // ---------------------------------------------------------
    // Guardar usuario de Google en Realtime Database
    // ---------------------------------------------------------
    private void guardarUsuarioGoogle(FirebaseUser firebaseUser, OnRegisterResult callback) {
        if (firebaseUser == null) {
            callback.onError(new Exception("FirebaseUser nulo al guardar perfil de Google"));
            return;
        }

        User user = new User(
                firebaseUser.getUid(),
                firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "Usuario",
                firebaseUser.getEmail()
        );

        if (firebaseUser.getPhotoUrl() != null) {
            user.setFotoPerfil(firebaseUser.getPhotoUrl().toString());
        }

        getProfileRef(firebaseUser.getUid()).setValue(user)
                .addOnSuccessListener(aVoid -> {
                    android.util.Log.d("GOOGLE_LOGIN", "Usuario Google guardado correctamente en DB");
                    callback.onSuccess(firebaseUser);
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("GOOGLE_LOGIN", "Error guardando usuario Google: " + e.getMessage(), e);
                    callback.onError(e);
                });
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

    // ---------------------------------------------------------
    // Verificar si hay sesión activa
    // ---------------------------------------------------------
    public boolean isUserLoggedIn() {
        return auth.getCurrentUser() != null;
    }

    // ---------------------------------------------------------
    // Callback interno para verificación de baneo
    // ---------------------------------------------------------
    private interface OnBanCheckCallback {
        void onBanned(boolean isBanned, String motivo);
        void onError(Exception e);
    }
}