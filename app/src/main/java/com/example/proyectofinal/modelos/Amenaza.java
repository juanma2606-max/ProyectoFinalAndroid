package com.example.proyectofinal.modelos;

import java.util.ArrayList;
import java.util.List;

public class Amenaza {
    public String id;
    public String nombre;
    public String descripcion;
    public String tipo;
    public String imagen;
    public List<String> sintomas;
    public String tratamiento;

    // Constructor vacío necesario para Firebase
    public Amenaza() {
    }

    // Constructor básico (sin tratamiento)
    public Amenaza(String id, String nombre, String descripcion, String tipo,
                   String imagen, List<String> sintomas) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.tipo = tipo;
        this.imagen = imagen;
        this.sintomas = sintomas != null ? sintomas : new ArrayList<>();
        this.tratamiento = null;
    }

    // Constructor completo (con tratamiento)
    public Amenaza(String id, String nombre, String descripcion, String tipo,
                   String imagen, List<String> sintomas, String tratamiento) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.tipo = tipo;
        this.imagen = imagen;
        this.sintomas = sintomas != null ? sintomas : new ArrayList<>();
        this.tratamiento = tratamiento;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getTipo() {
        return tipo;
    }

    public String getImagen() {
        return imagen;
    }

    public List<String> getSintomas() {
        return sintomas != null ? sintomas : new ArrayList<>();
    }

    public String getTratamiento() {
        return tratamiento;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public void setSintomas(List<String> sintomas) {
        this.sintomas = sintomas;
    }

    public void setTratamiento(String tratamiento) {
        this.tratamiento = tratamiento;
    }

    // Métodos de utilidad
    public String getTipoTexto() {
        switch (tipo != null ? tipo : "") {
            case "plaga":
                return "Plaga";
            case "enfermedad":
                return "Enfermedad";
            default:
                return tipo;
        }
    }

    public String getTipoEmoji() {
        switch (tipo != null ? tipo : "") {
            case "plaga":
                return "🐛";
            case "enfermedad":
                return "🦠";
            default:
                return "⚠️";
        }
    }

    public int getTipoColor() {
        // Retorna color según tipo
        switch (tipo != null ? tipo : "") {
            case "plaga":
                return 0xFFDC3545; // Rojo (danger)
            case "enfermedad":
                return 0xFFFFC107; // Amarillo (warning)
            default:
                return 0xFF6C757D; // Gris (secondary)
        }
    }

    public int getTipoColorFondo() {
        // Retorna color fondo suave
        switch (tipo != null ? tipo : "") {
            case "plaga":
                return 0x33DC3545; // Rojo suave
            case "enfermedad":
                return 0x33FFC107; // Amarillo suave
            default:
                return 0x336C757D; // Gris suave
        }
    }

    public boolean tieneSintomas() {
        return sintomas != null && !sintomas.isEmpty();
    }

    public boolean tieneTratamiento() {
        return tratamiento != null && !tratamiento.isEmpty();
    }

    public String getSintomasTexto() {
        if (sintomas == null || sintomas.isEmpty()) {
            return "Sin síntomas registrados";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < sintomas.size(); i++) {
            sb.append("• ").append(sintomas.get(i));
            if (i < sintomas.size() - 1) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    public int getNumeroSintomas() {
        return sintomas != null ? sintomas.size() : 0;
    }

    @Override
    public String toString() {
        return "Amenaza{" +
                "id='" + id + '\'' +
                ", nombre='" + nombre + '\'' +
                ", tipo='" + tipo + '\'' +
                ", sintomas=" + (sintomas != null ? sintomas.size() : 0) +
                ", tieneTratamiento=" + tieneTratamiento() +
                '}';
    }
}