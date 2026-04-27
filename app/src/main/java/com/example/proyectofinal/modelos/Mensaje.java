package com.example.proyectofinal.modelos;

public class Mensaje {

    private String role;    // "user" o "assistant"
    private String content;

    public Mensaje() {
        // Constructor vacío para Firebase (si decides guardarlo)
    }

    public Mensaje(String role, String content) {
        this.role = role;
        this.content = content;
    }

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