package com.example.proyectofinal.modelos;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Huerto {
    private String id;
    private String nombre;
    private String descripcion;
    private String foto;
    private String ubicacion;
    private double superficie;
    private String tipo_suelo;
    private int horas_sol;
    private boolean tiene_riego;
    private String notas;
    private String fecha_creacion;

    // Constructor vacío necesario para Firebase
    public Huerto() {
    }

    // Constructor básico (campos mínimos)
    public Huerto(String id, String nombre, String descripcion) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.foto = null;
        this.ubicacion = "";
        this.superficie = 0.0;
        this.tipo_suelo = "franco";
        this.horas_sol = 6;
        this.tiene_riego = false;
        this.notas = "";
        this.fecha_creacion = getCurrentDate();
    }

    // Constructor completo
    public Huerto(String id, String nombre, String descripcion, String foto,
                  String ubicacion, double superficie, String tipo_suelo,
                  int horas_sol, boolean tiene_riego, String notas,
                  String fecha_creacion) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.foto = foto;
        this.ubicacion = ubicacion;
        this.superficie = superficie;
        this.tipo_suelo = tipo_suelo;
        this.horas_sol = horas_sol;
        this.tiene_riego = tiene_riego;
        this.notas = notas;
        this.fecha_creacion = fecha_creacion != null ? fecha_creacion : getCurrentDate();
    }

    // Getters - nombres EXACTOS para Firebase
    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public String getFoto() { return foto; }
    public String getUbicacion() { return ubicacion; }
    public double getSuperficie() { return superficie; }
    public String getTipo_suelo() { return tipo_suelo; }
    public int getHoras_sol() { return horas_sol; }
    public boolean getTiene_riego() { return tiene_riego; }
    public String getNotas() { return notas; }
    public String getFecha_creacion() { return fecha_creacion; }

    // Setters - nombres EXACTOS para Firebase
    public void setId(String id) { this.id = id; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void setFoto(String foto) { this.foto = foto; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }
    public void setSuperficie(double superficie) { this.superficie = superficie; }
    public void setTipo_suelo(String tipo_suelo) { this.tipo_suelo = tipo_suelo; }
    public void setHoras_sol(int horas_sol) { this.horas_sol = horas_sol; }
    public void setTiene_riego(boolean tiene_riego) { this.tiene_riego = tiene_riego; }
    public void setNotas(String notas) { this.notas = notas; }
    public void setFecha_creacion(String fecha_creacion) { this.fecha_creacion = fecha_creacion; }

    // Métodos de utilidad
    public String getSuperficieTexto() {
        return superficie + " m²";
    }

    public String getHorasSolTexto() {
        return horas_sol + " horas/día";
    }

    public String getRiegoTexto() {
        return tiene_riego ? "Automatizado" : "Manual";
    }

    public String getTipoSueloCapitalizado() {
        if (tipo_suelo == null || tipo_suelo.isEmpty()) return "";
        return tipo_suelo.substring(0, 1).toUpperCase() + tipo_suelo.substring(1);
    }

    public boolean tieneFoto() {
        return foto != null && !foto.isEmpty();
    }

    public boolean tieneNotas() {
        return notas != null && !notas.isEmpty();
    }

    private static String getCurrentDate() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                java.util.Locale.getDefault()).format(new java.util.Date());
    }

    @Override
    public String toString() {
        return "Huerto{" +
                "id='" + id + '\'' +
                ", nombre='" + nombre + '\'' +
                ", ubicacion='" + ubicacion + '\'' +
                ", superficie=" + superficie +
                ", tipo_suelo='" + tipo_suelo + '\'' +
                ", horas_sol=" + horas_sol +
                ", tiene_riego=" + tiene_riego +
                '}';
    }
}