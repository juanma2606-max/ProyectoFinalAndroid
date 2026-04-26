package com.example.proyectofinal.modelos;

public class Huerto {
    public String id;
    public String nombre;
    public String descripcion;
    public String foto;
    public String ubicacion;
    public double superficie;
    public String tipo_suelo;
    public int horas_sol;
    public boolean tiene_riego;
    public String notas;
    public String fecha_creacion;

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

    public String getFoto() {
        return foto;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public double getSuperficie() {
        return superficie;
    }

    public String getTipoSuelo() {
        return tipo_suelo;
    }

    public int getHorasSol() {
        return horas_sol;
    }

    public boolean isTieneRiego() {
        return tiene_riego;
    }

    public String getNotas() {
        return notas;
    }

    public String getFechaCreacion() {
        return fecha_creacion;
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

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public void setSuperficie(double superficie) {
        this.superficie = superficie;
    }

    public void setTipoSuelo(String tipo_suelo) {
        this.tipo_suelo = tipo_suelo;
    }

    public void setHorasSol(int horas_sol) {
        this.horas_sol = horas_sol;
    }

    public void setTieneRiego(boolean tiene_riego) {
        this.tiene_riego = tiene_riego;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }

    public void setFechaCreacion(String fecha_creacion) {
        this.fecha_creacion = fecha_creacion;
    }

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

    // Obtener fecha actual en formato ISO
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