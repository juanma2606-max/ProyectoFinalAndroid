package com.example.proyectofinal.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.example.proyectofinal.R;
import com.example.proyectofinal.dao.UserDAO;
import com.example.proyectofinal.fragments.AdminFragment;
import com.example.proyectofinal.fragments.AjustesFragment;
import com.example.proyectofinal.fragments.AmenazaFragment;
import com.example.proyectofinal.fragments.HomeFragment;
import com.example.proyectofinal.fragments.PlantaFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class ContenedorActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private UserDAO userDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contenedor);

        bottomNav = findViewById(R.id.bottomNav);

        userDAO = new UserDAO();
        String email = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getEmail()
                : null;

        if ("admin@huerting.com".equals(email)) {
            runOnUiThread(() -> {
                MenuItem adminItem = bottomNav.getMenu().findItem(R.id.nav_admin);
                if (adminItem != null) adminItem.setVisible(true);
            });
        }

        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                fragment = new HomeFragment();
            } else if (id == R.id.nav_plantas) {
                fragment = new PlantaFragment();
            } else if (id == R.id.nav_chat) {
                Intent i = new Intent(this, ChatActivity.class);
                startActivity(i);
                return true;
            } else if (id == R.id.nav_amenazas) {
                fragment = new AmenazaFragment();
            } else if (id == R.id.nav_ajustes) {
                fragment = new AjustesFragment();
            } else if (id == R.id.nav_admin) {
                fragment = new AdminFragment();
            }

            return loadFragment(fragment);
        });
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.nav_host_fragment, fragment)
                    .commit();
            return true;
        }
        return false;
    }
}