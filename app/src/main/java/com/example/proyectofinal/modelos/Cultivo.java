package com.example.proyectofinal.modelos;

public class Cultivo {

    public String id;
    public String plantaId;
    public String fechaSiembra;
    public String notas;
    public String estado;   // "creciendo", "cosechado", "perdido"

    // Constructor vacío requerido por Firebase
    public Cultivo() {}

    public Cultivo(String id, String plantaId, String fechaSiembra, String notas, String estado) {
        this.id           = id;
        this.plantaId     = plantaId;
        this.fechaSiembra = fechaSiembra;
        this.notas        = notas;
        this.estado       = estado;
    }
}