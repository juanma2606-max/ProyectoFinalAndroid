package com.example.proyectofinal.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import android.content.Intent;
import android.os.Bundle;

import com.example.proyectofinal.MusicService;
import com.example.proyectofinal.R;
import com.example.proyectofinal.fragments.AjustesFragment;
import com.example.proyectofinal.fragments.AmenazaFragment;
import com.example.proyectofinal.fragments.HomeFragment;
import com.example.proyectofinal.fragments.PlantaFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ContenedorActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private boolean musicEnabled = true ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contenedor);

        //Iniciar música SOLO si está activada (ej: desde SharedPreferences)
        if (musicEnabled) {
            Intent musicIntent = new Intent(this, MusicService.class);
            startService(musicIntent);
            MusicService.startMusic(this, R.raw.background_music);
        }

        bottomNav = findViewById(R.id.bottomNav);

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
            } else if (id == R.id.nav_amenazas) {
                fragment = new AmenazaFragment();
            }else if (id == R.id.nav_ajustes) {
                fragment = new AjustesFragment();
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

    //Detener música al cerrar la app
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (musicEnabled) {
            Intent musicIntent = new Intent(this, MusicService.class);
            stopService(musicIntent);
            MusicService.stopMusic();
        }
    }

    // Pausar música si la app va a segundo plano
    @Override
    protected void onPause() {
        super.onPause();
        if (musicEnabled) {
            MusicService.pauseMusic();
        }
    }

    //Reanudar música al volver a la app
    @Override
    protected void onResume() {
        super.onResume();
        if (musicEnabled) {
            MusicService.resumeMusic();
        }
    }
}