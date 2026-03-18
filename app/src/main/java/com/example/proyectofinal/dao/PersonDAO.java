package com.example.proyectofinal.dao;

import com.example.proyectofinal.modelos.Person;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PersonDAO {

    private final DatabaseReference usersRef;
    private final FirebaseAuth auth;

    public PersonDAO() {
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
    // Crear un usuario en Firebase
    // ---------------------------------------------------------
    public void createPerson(Person person) {
        usersRef.child(person.uid).setValue(person);
    }

    // ---------------------------------------------------------
    // Actualizar un usuario
    // ---------------------------------------------------------
    public void updatePerson(Person person) {
        usersRef.child(person.uid).setValue(person);
    }

    // ---------------------------------------------------------
    // Eliminar un usuario
    // ---------------------------------------------------------
    public void removePerson(String uid) {
        usersRef.child(uid).removeValue();
    }

    // ---------------------------------------------------------
    // Obtener usuario autenticado
    // ---------------------------------------------------------
    public FirebaseUser getUserAuth() {
        return auth.getCurrentUser();
    }

    // ---------------------------------------------------------
    // Registrar usuario con email y contraseña (movido desde SignInActivity)
    // ---------------------------------------------------------
    public void registerWithEmail(String email, String password, String username,
                                  OnRegisterCallback callback) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    FirebaseUser user = result.getUser();
                    if (user == null) {
                        callback.onError(new Exception("Usuario nulo tras registro"));
                        return;
                    }
                    Person person = new Person(user.getUid(), username, email);
                    createPerson(person);
                    callback.onSuccess(user);
                })
                .addOnFailureListener(callback::onError);
    }

    // ---------------------------------------------------------
    // Callbacks
    // ---------------------------------------------------------
    public interface OnRegisterCallback {
        void onSuccess(FirebaseUser user);
        void onError(Exception e);
    }
}