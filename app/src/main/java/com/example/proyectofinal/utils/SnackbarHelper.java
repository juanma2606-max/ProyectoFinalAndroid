package com.example.proyectofinal.utils;

import android.app.Activity;
import android.view.View;
import android.graphics.Color;
import androidx.fragment.app.Fragment;
import com.google.android.material.snackbar.Snackbar;

public class SnackbarHelper {

    public static void show(Activity activity, String mensaje) {
        View root = activity.findViewById(android.R.id.content);
        Snackbar.make(root, mensaje, Snackbar.LENGTH_SHORT).show();
    }

    public static void showError(Activity activity, String mensaje) {
        View root = activity.findViewById(android.R.id.content);
        Snackbar snackbar = Snackbar.make(root, mensaje, Snackbar.LENGTH_SHORT);
        snackbar.getView().setBackgroundColor(Color.parseColor("#C62828"));
        snackbar.show();
    }

    public static void showSuccess(Activity activity, String mensaje) {
        View root = activity.findViewById(android.R.id.content);
        Snackbar snackbar = Snackbar.make(root, mensaje, Snackbar.LENGTH_SHORT);
        snackbar.getView().setBackgroundColor(Color.parseColor("#2E7D32"));
        snackbar.show();
    }

    public static void show(Fragment fragment, String mensaje) {
        if (fragment.getView() != null) {
            Snackbar.make(fragment.getView(), mensaje, Snackbar.LENGTH_SHORT).show();
        }
    }

    public static void showError(Fragment fragment, String mensaje) {
        if (fragment.getView() != null) {
            Snackbar snackbar = Snackbar.make(fragment.getView(), mensaje, Snackbar.LENGTH_SHORT);
            snackbar.getView().setBackgroundColor(Color.parseColor("#C62828"));
            snackbar.show();
        }
    }

    public static void showSuccess(Fragment fragment, String mensaje) {
        if (fragment.getView() != null) {
            Snackbar snackbar = Snackbar.make(fragment.getView(), mensaje, Snackbar.LENGTH_SHORT);
            snackbar.getView().setBackgroundColor(Color.parseColor("#2E7D32"));
            snackbar.show();
        }
    }
}