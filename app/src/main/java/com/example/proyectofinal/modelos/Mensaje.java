package com.example.proyectofinal.modelos;

public class Mensaje {

    private String role;    // "user" o "assistant"
    private String content;
    private String imageUrl; // URL de Cloudinary, null si no hay imagen


    public Mensaje() {
        // Constructor vacío para Firebase (si decides guardarlo)
    }

    public Mensaje(String role, String content) {
        this.role = role;
        this.content = content;
    }
    public Mensaje(String role, String content, String imageUrl) {
        this.role = role;
        this.content = content;
        this.imageUrl = imageUrl;
    }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public boolean tieneImagen() { return imageUrl != null && !imageUrl.isEmpty(); }

    // Getters
    public String getRole() {
        return role;
    }

    public String getContent() {
        return content;
    }

    // Setters
    public void setRole(String role) {
        this.role = role;
    }

    public void setContent(String content) {
        this.content = content;
    }

    // Helpers
    public boolean esUsuario() {
        return "user".equals(role);
    }

    public boolean esAsistente() {
        return "assistant".equals(role);
    }

    @Override
    public String toString() {
        return "Mensaje{" +
                "role='" + role + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}