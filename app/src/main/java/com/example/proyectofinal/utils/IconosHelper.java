package com.example.proyectofinal.utils;

public class IconosHelper {

    /**
     * Obtener color de fondo según tipo de amenaza
     */
    public static int getColorAmenaza(String tipo) {
        if ("plaga".equalsIgnoreCase(tipo)) {
            return 0xFFFF6B6B; // Rojo
        } else if ("enfermedad".equalsIgnoreCase(tipo)) {
            return 0xFFFFD93D; // Amarillo
        } else {
            return 0xFFCCCCCC; // Gris
        }
    }

    /**
     * Obtener icono según tipo de amenaza
     * Usa iconos de Font Awesome o Material Icons si los tienes
     * Si no, usa iconos del sistema Android
     */
    public static int getIconoAmenaza(String tipo) {
        if ("plaga".equalsIgnoreCase(tipo)) {
            return android.R.drawable.ic_delete; // 🐛 (puedes cambiarlo por Font Awesome)
        } else if ("enfermedad".equalsIgnoreCase(tipo)) {
            return android.R.drawable.ic_dialog_alert; // 🦠
        } else {
            return android.R.drawable.ic_menu_help;
        }
    }

    /**
     * Obtener color según tipo de planta
     */
    public static int getColorPlanta(String tipo) {
        if (tipo == null) return 0xFF9E9E9E;

        switch (tipo.toLowerCase()) {
            case "hortaliza":
                return 0xFF4CAF50; // Verde
            case "fruta":
                return 0xFFE91E63; // Rosa
            case "hierba":
                return 0xFF8BC34A; // Verde claro
            case "flor":
                return 0xFFFF9800; // Naranja
            case "arbol":
                return 0xFF795548; // Marrón
            default:
                return 0xFF9E9E9E; // Gris
        }
    }

    /**
     * Obtener icono según tipo de planta
     */
    public static int getIconoPlanta(String tipo) {
        if (tipo == null) return android.R.drawable.ic_menu_help;

        switch (tipo.toLowerCase()) {
            case "hortaliza":
                return android.R.drawable.ic_menu_agenda; // 🥬
            case "fruta":
                return android.R.drawable.ic_menu_gallery; // 🍎
            case "hierba":
                return android.R.drawable.ic_menu_compass; // 🌿
            case "flor":
                return android.R.drawable.ic_menu_view; // 🌸
            case "arbol":
                return android.R.drawable.star_on; // 🌳
            default:
                return android.R.drawable.ic_menu_help;
        }
    }
}