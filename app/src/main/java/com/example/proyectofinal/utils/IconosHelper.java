package com.example.proyectofinal.utils;

import com.example.proyectofinal.R;

/**
 * Helper para obtener icono y color de fondo según el tipo de amenaza,
 * usado como fallback cuando no hay imagen disponible en Firebase.
 */
public class IconosHelper {

    /**
     * Devuelve el drawable correspondiente al tipo de amenaza.
     * - "plaga"      → ic_bug
     * - "enfermedad" → ic_amenazas
     * - cualquier otro → ic_bug (fallback genérico)
     */
    public static int getIconoAmenaza(String tipo) {
        if (tipo == null) return R.drawable.ic_bug;
        switch (tipo.toLowerCase()) {
            case "enfermedad":
                return R.drawable.ic_amenazas;
            case "plaga":
            default:
                return R.drawable.ic_bug;
        }
    }

    /**
     * Devuelve el color de fondo a aplicar en el ImageView según el tipo.
     * - "plaga"      → rojo suave
     * - "enfermedad" → amarillo suave
     * - cualquier otro → gris suave
     */
    public static int getColorAmenaza(String tipo) {
        if (tipo == null) return 0x336C757D;
        switch (tipo.toLowerCase()) {
            case "plaga":
                return 0x33DC3545; // Rojo suave
            case "enfermedad":
                return 0x33FFC107; // Amarillo suave
            default:
                return 0x336C757D; // Gris suave
        }
    }
}