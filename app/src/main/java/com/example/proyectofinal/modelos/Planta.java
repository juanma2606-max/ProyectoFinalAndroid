package com.example.proyectofinal.modelos;

import java.util.ArrayList;
import java.util.List;

public class Planta {
    public String id;
    public String nombre;
    public String nombre_cientifico;
    public String descripcion;
    public String tipo;
    public String imagen;
    public String estacion;
    public int tiempo_crecimiento;
    public String riego;
    public String luz;
    public String abono;
    public List<String> incompatibilidades;
    public List<String> amenazas;

    // Constructor vacío necesario para Firebase
    public Planta() {
    }

    // Constructor básico (sin nombre científico)
    public Planta(String id, String nombre, String descripcion, String tipo,
                  String imagen, String estacion, int tiempo_crecimiento,
                  String riego, String luz, String abono,
                  List<String> incompatibilidades, List<String> amenazas) {
        this.id = id;
        this.nombre = nombre;
        this.nombre_cientifico = null;
        this.descripcion = descripcion;
        this.tipo = tipo;
        this.imagen = imagen;
        this.estacion = estacion;
        this.tiempo_crecimiento = tiempo_crecimiento;
        this.riego = riego;
        this.luz = luz;
        this.abono = abono;
        this.incompatibilidades = incompatibilidades != null ? incompatibilidades : new ArrayList<>();
        this.amenazas = amenazas != null ? amenazas : new ArrayList<>();
    }

    // Constructor completo (con nombre científico)
    public Planta(String id, String nombre, String nombre_cientifico, String descripcion,
                  String tipo, String imagen, String estacion, int tiempo_crecimiento,
                  String riego, String luz, String abono,
                  List<String> incompatibilidades, List<String> amenazas) {
        this.id = id;
        this.nombre = nombre;
        this.nombre_cientifico = nombre_cientifico;
        this.descripcion = descripcion;
        this.tipo = tipo;
        this.imagen = imagen;
        this.estacion = estacion;
        this.tiempo_crecimiento = tiempo_crecimiento;
        this.riego = riego;
        this.luz = luz;
        this.abono = abono;
        this.incompatibilidades = incompatibilidades != null ? incompatibilidades : new ArrayList<>();
        this.amenazas = amenazas != null ? amenazas : new ArrayList<>();
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getNombreCientifico() {
        return nombre_cientifico;
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

    public String getEstacion() {
        return estacion;
    }

    public int getTiempoCrecimiento() {
        return tiempo_crecimiento;
    }

    public String getRiego() {
        return riego;
    }

    public String getLuz() {
        return luz;
    }

    public String getAbono() {
        return abono;
    }

    public List<String> getIncompatibilidades() {
        return incompatibilidades != null ? incompatibilidades : new ArrayList<>();
    }

    public List<String> getAmenazas() {
        return amenazas != null ? amenazas : new ArrayList<>();
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setNombreCientifico(String nombre_cientifico) {
        this.nombre_cientifico = nombre_cientifico;
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

    public void setEstacion(String estacion) {
        this.estacion = estacion;
    }

    public void setTiempoCrecimiento(int tiempo_crecimiento) {
        this.tiempo_crecimiento = tiempo_crecimiento;
    }

    public void setRiego(String riego) {
        this.riego = riego;
    }

    public void setLuz(String luz) {
        this.luz = luz;
    }

    public void setAbono(String abono) {
        this.abono = abono;
    }

    public void setIncompatibilidades(List<String> incompatibilidades) {
        this.incompatibilidades = incompatibilidades;
    }

    public void setAmenazas(List<String> amenazas) {
        this.amenazas = amenazas;
    }

    // Métodos de utilidad
    public String getTiempoCrecimientoTexto() {
        return tiempo_crecimiento + " días";
    }

    public String getTipoCapitalizado() {
        if (tipo == null || tipo.isEmpty()) return "";
        return tipo.substring(0, 1).toUpperCase() + tipo.substring(1);
    }

    public boolean tieneIncompatibilidades() {
        return incompatibilidades != null && !incompatibilidades.isEmpty();
    }

    public boolean tieneAmenazas() {
        return amenazas != null && !amenazas.isEmpty();
    }

    @Override
    public String toString() {
        return "Planta{" +
                "id='" + id + '\'' +
                ", nombre='" + nombre + '\'' +
                ", nombre_cientifico='" + nombre_cientifico + '\'' +
                ", tipo='" + tipo + '\'' +
                ", estacion='" + estacion + '\'' +
                ", tiempo_crecimiento=" + tiempo_crecimiento +
                ", riego='" + riego + '\'' +
                ", luz='" + luz + '\'' +
                '}';
    }
}