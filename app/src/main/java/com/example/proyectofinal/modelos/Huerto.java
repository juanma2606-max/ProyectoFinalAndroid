package com.example.proyectofinal.modelos;

public class Huerto {

    public String id;
    public String nombre;
    public String descripcion;
    public String fechaInicio;
    public String tipo;  // "parcela" o "maceta"

    // Constructor vacío requerido por Firebase
    public Huerto() {}

    public Huerto(String id, String nombre, String descripcion, String tipo, String fechaInicio) {
        this.id          = id;
        this.nombre      = nombre;
        this.descripcion = descripcion;
        this.tipo        = tipo;
        this.fechaInicio = fechaInicio;
    }
}