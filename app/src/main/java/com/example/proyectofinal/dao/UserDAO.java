package com.example.proyectofinal.dao;

import com.example.proyectofinal.modelos.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

public class UserDAO {

    private final DatabaseReference usersRef;
    private final FirebaseAuth auth;

    public UserDAO() {
        this.auth = FirebaseAuth.getInstance();
        this.usersRef = FirebaseDatabase.getInstance().getReference("users");
    }

    // ---------------------------------------------------------
    // Obtener todos los usuarios
    // ---------------------------------------------------------
    public void getAllPersons(ValueEventListener listener) {
        usersRef.addValueEventListener(listener);
    }

    // ---------------------------------------------------------
    // Obtener un usuario por ID
    // ---------------------------------------------------------
    public void getPersonById(String uid, ValueEventListener listener) {
        usersRef.child(uid).addListenerForSingleValueEvent(listener);
    }

    // ---------------------------------------------------------
    // Verificar si usuario es admin
    // ---------------------------------------------------------
    public void isAdmin(String uid, OnAdminCheckCallback callback) {
        usersRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    callback.onResult(user.isAdmin());
                } else {
                    callback.onResult(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onResult(false);
            }
        });
    }

    // ---------------------------------------------------------
    // Verificar si usuario está baneado
    // ---------------------------------------------------------
    public void isBanned(String uid, OnBanCheckCallback callback) {
        usersRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null && user.isBaneado()) {
                    callback.onBanned(true, user.getMotivoBaneo());
                } else {
                    callback.onBanned(false, null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onBanned(false, null);
            }
        });
    }

    // ---------------------------------------------------------
    // Crear un usuario en Firebase
    // ---------------------------------------------------------
    public void createPerson(User person) {
        usersRef.child(person.uid).setValue(person);
    }

    // ---------------------------------------------------------
    // Crear usuario con callback
    // ---------------------------------------------------------
    public void createPerson(User person, OnOperationCallback callback) {
        usersRef.child(person.uid).setValue(person)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    // ---------------------------------------------------------
    // Actualizar un usuario
    // ---------------------------------------------------------
    public void updatePerson(User person) {
        usersRef.child(person.uid).setValue(person);
    }

    // ---------------------------------------------------------
    // Actualizar usuario con callback
    // ---------------------------------------------------------
    public void updatePerson(User person, OnOperationCallback callback) {
        usersRef.child(person.uid).setValue(person)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    // ---------------------------------------------------------
    // Banear usuario
    // ---------------------------------------------------------
    public void banUser(String uid, String motivo, OnOperationCallback callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("baneado", true);
        updates.put("motivoBaneo", motivo);

        usersRef.child(uid).updateChildren(updates)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    // ---------------------------------------------------------
    // Desbanear usuario
    // ---------------------------------------------------------
    public void unbanUser(String uid, OnOperationCallback callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("baneado", false);
        updates.put("motivoBaneo", null);

        usersRef.child(uid).updateChildren(updates)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    // ---------------------------------------------------------
    // Hacer admin a usuario
    // ---------------------------------------------------------
    public void makeAdmin(String uid, OnOperationCallback callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("admin", true);

        usersRef.child(uid).updateChildren(updates)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    // ---------------------------------------------------------
    // Quitar admin a usuario
    // ---------------------------------------------------------
    public void removeAdmin(String uid, OnOperationCallback callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("admin", false);

        usersRef.child(uid).updateChildren(updates)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    // ---------------------------------------------------------
    // Eliminar un usuario completamente
    // ---------------------------------------------------------
    public void removePerson(String uid) {
        usersRef.child(uid).removeValue();
    }

    // ---------------------------------------------------------
    // Eliminar usuario con callback
    // ---------------------------------------------------------
    public void removePerson(String uid, OnOperationCallback callback) {
        // Primero banear permanentemente
        banUser(uid, "Usuario eliminado permanentemente", new OnOperationCallback() {
            @Override
            public void onSuccess() {
                // Luego eliminar sus datos
                usersRef.child(uid).removeValue()
                        .addOnSuccessListener(aVoid -> callback.onSuccess())
                        .addOnFailureListener(callback::onError);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }

    // ---------------------------------------------------------
    // Obtener usuario autenticado
    // ---------------------------------------------------------
    public FirebaseUser getUserAuth() {
        return auth.getCurrentUser();
    }

    // ---------------------------------------------------------
    // Registrar usuario con email y contraseña
    // ---------------------------------------------------------
    public void registerWithEmail(String email, String password, String username,
                                  OnRegisterCallback callback) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    FirebaseUser firebaseUser = result.getUser();
                    if (firebaseUser == null) {
                        callback.onError(new Exception("Usuario nulo tras registro"));
                        return;
                    }

                    // Crear usuario en database
                    User user = new User(firebaseUser.getUid(), username, email);
                    createPerson(user, new OnOperationCallback() {
                        @Override
                        public void onSuccess() {
                            callback.onSuccess(firebaseUser);
                        }

                        @Override
                        public void onError(Exception e) {
                            callback.onError(e);
                        }
                    });
                })
                .addOnFailureListener(callback::onError);
    }

    // ---------------------------------------------------------
    // Login con email y contraseña
    // ---------------------------------------------------------
    public void loginWithEmail(String email, String password, OnLoginCallback callback) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    FirebaseUser firebaseUser = result.getUser();
                    if (firebaseUser == null) {
                        callback.onError(new Exception("Usuario nulo tras login"));
                        return;
                    }

                    // Verificar si está baneado
                    isBanned(firebaseUser.getUid(), new OnBanCheckCallback() {
                        @Override
                        public void onBanned(boolean isBanned, String motivo) {
                            if (isBanned) {
                                // Cerrar sesión inmediatamente
                                auth.signOut();
                                callback.onBanned(motivo);
                            } else {
                                callback.onSuccess(firebaseUser);
                            }
                        }
                    });
                })
                .addOnFailureListener(callback::onError);
    }

    // ---------------------------------------------------------
    // Cerrar sesión
    // ---------------------------------------------------------
    public void logout() {
        auth.signOut();
    }

    // ---------------------------------------------------------
    // Callbacks
    // ---------------------------------------------------------
    public interface OnRegisterCallback {
        void onSuccess(FirebaseUser user);
        void onError(Exception e);
    }

    public interface OnLoginCallback {
        void onSuccess(FirebaseUser user);
        void onBanned(String motivo);
        void onError(Exception e);
    }

    public interface OnOperationCallback {
        void onSuccess();
        void onError(Exception e);
    }

    public interface OnAdminCheckCallback {
        void onResult(boolean isAdmin);
    }

    public interface OnBanCheckCallback {
        void onBanned(boolean isBanned, String motivo);
    }
}