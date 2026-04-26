package com.example.proyectofinal.modelos;

public class Cultivo {
    public String id;
    public String nombre;
    public String plantaId;
    public String fecha_siembra;
    public int cantidad;
    public String estado;
    public String amenazaId;
    public String notas;

    // Constructor vacío necesario para Firebase
    public Cultivo() {
    }

    // Constructor básico (sin amenaza)
    public Cultivo(String id, String nombre, String plantaId, String fecha_siembra,
                   int cantidad, String estado, String notas) {
        this.id = id;
        this.nombre = nombre;
        this.plantaId = plantaId;
        this.fecha_siembra = fecha_siembra;
        this.cantidad = cantidad;
        this.estado = estado;
        this.amenazaId = null;
        this.notas = notas;
    }

    // Constructor completo (con amenaza)
    public Cultivo(String id, String nombre, String plantaId, String fecha_siembra,
                   int cantidad, String estado, String amenazaId, String notas) {
        this.id = id;
        this.nombre = nombre;
        this.plantaId = plantaId;
        this.fecha_siembra = fecha_siembra;
        this.cantidad = cantidad;
        this.estado = estado;
        this.amenazaId = amenazaId;
        this.notas = notas;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getPlantaId() {
        return plantaId;
    }

    public String getFechaSiembra() {
        return fecha_siembra;
    }

    public int getCantidad() {
        return cantidad;
    }

    public String getEstado() {
        return estado;
    }

    public String getAmenazaId() {
        return amenazaId;
    }

    public String getNotas() {
        return notas;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setPlantaId(String plantaId) {
        this.plantaId = plantaId;
    }

    public void setFechaSiembra(String fecha_siembra) {
        this.fecha_siembra = fecha_siembra;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public void setAmenazaId(String amenazaId) {
        this.amenazaId = amenazaId;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }

    // Métodos de utilidad
    public String getCantidadTexto() {
        return cantidad + " planta" + (cantidad != 1 ? "s" : "");
    }

    public String getEstadoTexto() {
        switch (estado != null ? estado : "") {
            case "plantado":
                return "Plantado";
            case "creciendo":
                return "Creciendo";
            case "maduro":
                return "Maduro";
            case "cosechado":
                return "Cosechado";
            case "enfermo":
                return "Enfermo";
            default:
                return estado;
        }
    }

    public int getEstadoColor() {
        // Retorna color según estado (usar en Android)
        switch (estado != null ? estado : "") {
            case "plantado":
                return 0xFF6C757D; // Gris (secondary)
            case "creciendo":
                return 0xFF0DCAF0; // Azul (info)
            case "maduro":
                return 0xFF198754; // Verde (success)
            case "cosechado":
                return 0xFF212529; // Negro (dark)
            case "enfermo":
                return 0xFFFFC107; // Amarillo (warning)
            default:
                return 0xFF6C757D; // Gris por defecto
        }
    }

    public int getEstadoIcono() {
        // Retorna icono según estado (usar FontAwesome o Material Icons)
        switch (estado != null ? estado : "") {
            case "plantado":
                return android.R.drawable.ic_menu_add; // Seedling
            case "creciendo":
                return android.R.drawable.ic_menu_info_details; // Leaf
            case "maduro":
                return android.R.drawable.ic_menu_compass; // Check-circle
            case "cosechado":
                return android.R.drawable.ic_menu_save; // Box
            case "enfermo":
                return android.R.drawable.ic_dialog_alert; // Virus
            default:
                return android.R.drawable.ic_menu_help;
        }
    }

    public boolean estaEnfermo() {
        return "enfermo".equals(estado);
    }

    public boolean estaCosechado() {
        return "cosechado".equals(estado);
    }

    public boolean tieneAmenaza() {
        return amenazaId != null && !amenazaId.isEmpty();
    }

    public boolean tieneNotas() {
        return notas != null && !notas.isEmpty();
    }

    @Override
    public String toString() {
        return "Cultivo{" +
                "id='" + id + '\'' +
                ", nombre='" + nombre + '\'' +
                ", plantaId='" + plantaId + '\'' +
                ", fecha_siembra='" + fecha_siembra + '\'' +
                ", cantidad=" + cantidad +
                ", estado='" + estado + '\'' +
                ", amenazaId='" + amenazaId + '\'' +
                '}';
    }
}