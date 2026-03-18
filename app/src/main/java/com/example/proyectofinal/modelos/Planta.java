package com.example.proyectofinal.modelos;

import java.util.List;

public class Planta {
    public String id;
    public String nombre;
    public String imagen;
    public String descripcion;
    public String tipo;
    public String estacion;
    public String abono;
    public String riego;
    public String tiempoCrecimiento;
    public List<String> incompatibilidades;
    public List<String> plagas;

    public Planta() {} // Firebase necesita constructor vacío

    public Planta(String id, String nombre, String imagen, String descripcion,
                  String tipo, String estacion, String abono, String riego,
                  String tiempoCrecimiento, List<String> incompatibilidades,
                  List<String> plagas) {
        this.id = id;
        this.nombre = nombre;
        this.imagen = imagen;
        this.descripcion = descripcion;
        this.tipo = tipo;
        this.estacion = estacion;
        this.abono = abono;
        this.riego = riego;
        this.tiempoCrecimiento = tiempoCrecimiento;
        this.incompatibilidades = incompatibilidades;
        this.plagas = plagas;
    }
}
