package com.example.proyectofinal;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

public class MusicService extends Service {

    private static MediaPlayer mediaPlayer;
    private static boolean isPlaying = false;

    @Override
    public void onCreate() {
        super.onCreate();
        // La música se inicia en el método startMusic
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY; // Mantiene el servicio activo
    }

    @Override
    public void onDestroy() {
        stopMusic();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // 🔊 Iniciar música (loop infinito)
    public static void startMusic(Context context, int musicResId) {
        if (isPlaying) return; // Evitar múltiples instancias

        try {
            mediaPlayer = MediaPlayer.create(context, musicResId);
            mediaPlayer.setLooping(true); // Loop infinito
            mediaPlayer.setVolume(0.3f, 0.3f); // Volumen bajo (30%)
            mediaPlayer.start();
            isPlaying = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ⏹️ Detener música
    public static void stopMusic() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
            isPlaying = false;
        }
    }

    // ⏸️ Pausar música (ej: cuando llega una llamada)
    public static void pauseMusic() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    // ▶️ Reanudar música
    public static void resumeMusic() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    // 🎚️ Cambiar volumen
    public static void setVolume(float leftVolume, float rightVolume) {
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(leftVolume, rightVolume);
        }
    }
}