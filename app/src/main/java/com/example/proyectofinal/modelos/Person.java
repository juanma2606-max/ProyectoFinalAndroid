package com.example.proyectofinal.modelos;

public class Person {
    public String uid;
    public String username;
    public String email;

    public Person() {
        // Necesario para Firebase
    }

    public Person(String uid, String username, String email) {
        this.uid = uid;
        this.username = username;
        this.email = email;
    }
}
