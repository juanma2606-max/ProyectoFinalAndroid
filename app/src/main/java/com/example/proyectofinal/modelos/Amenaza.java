package com.example.proyectofinal.modelos;

import java.util.List;

public class Amenaza {
    public String id;
    public String nombre;
    public String tipo;        // "plaga" o "enfermedad"
    public String descripcion;
    public String imagen;
    public String tratamiento;
    public List<String> sintomas;

    public Amenaza() {}
}