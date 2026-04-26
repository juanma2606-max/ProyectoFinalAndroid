package com.example.proyectofinal.modelos;

public class User {
    public String uid;
    public String username;
    public String email;
    public Boolean admin;
    public Boolean baneado;
    public String motivoBaneo;

    // Constructor vacío necesario para Firebase
    public User() {
    }

    // Constructor básico
    public User(String uid, String username, String email) {
        this.uid = uid;
        this.username = username;
        this.email = email;
        this.admin = false;
        this.baneado = false;
        this.motivoBaneo = null;
    }

    // Constructor completo
    public User(String uid, String username, String email, Boolean admin, Boolean baneado, String motivoBaneo) {
        this.uid = uid;
        this.username = username;
        this.email = email;
        this.admin = admin != null ? admin : false;
        this.baneado = baneado != null ? baneado : false;
        this.motivoBaneo = motivoBaneo;
    }

    // Getters
    public String getUid() {
        return uid;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public Boolean getAdmin() {
        return admin != null ? admin : false;
    }

    public Boolean getBaneado() {
        return baneado != null ? baneado : false;
    }

    public String getMotivoBaneo() {
        return motivoBaneo;
    }

    // Setters
    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setAdmin(Boolean admin) {
        this.admin = admin;
    }

    public void setBaneado(Boolean baneado) {
        this.baneado = baneado;
    }

    public void setMotivoBaneo(String motivoBaneo) {
        this.motivoBaneo = motivoBaneo;
    }

    // Métodos de utilidad
    public boolean isAdmin() {
        return admin != null && admin;
    }

    public boolean isBaneado() {
        return baneado != null && baneado;
    }

    @Override
    public String toString() {
        return "User{" +
                "uid='" + uid + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", admin=" + admin +
                ", baneado=" + baneado +
                ", motivoBaneo='" + motivoBaneo + '\'' +
                '}';
    }
}