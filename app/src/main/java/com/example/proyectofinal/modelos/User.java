package com.example.proyectofinal.modelos;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {
    private String uid;
    private String username;
    private String email;
    private Boolean admin;
    private Boolean baneado;
    private String motivoBaneo;
    private String fotoPerfil;

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
        this.fotoPerfil = "avatar1.webp"; // Foto por defecto (actualizada de perfil1.png)
    }

    // Constructor completo
    public User(String uid, String username, String email, Boolean admin, Boolean baneado, String motivoBaneo) {
        this.uid = uid;
        this.username = username;
        this.email = email;
        this.admin = admin != null ? admin : false;
        this.baneado = baneado != null ? baneado : false;
        this.motivoBaneo = motivoBaneo;
        this.fotoPerfil = "avatar1.webp"; // Foto por defecto (actualizada de perfil1.png)
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

    public String getFotoPerfil() {
        return fotoPerfil;
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

    public void setFotoPerfil(String fotoPerfil) {
        this.fotoPerfil = fotoPerfil;
    }

    // Métodos de utilidad
    public boolean esAdmin() {
        return admin != null && admin;
    }

    public boolean estaBaneado() {
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
                ", fotoPerfil='" + fotoPerfil + '\'' +
                '}';
    }
}
