package com.example.proyectofinal.activities;

import android.content.Intent;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.proyectofinal.R;

public class PortadaActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener {

    private TextureView bgVideo;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_portada);

        bgVideo = findViewById(R.id.bgVideo);
        bgVideo.setSurfaceTextureListener(this);

        Button btnSignIn = findViewById(R.id.btnSignIn);
        Button btnLogin = findViewById(R.id.btnLogin);

        btnSignIn.setOnClickListener(v ->
                startActivity(new Intent(this, SignInActivity.class))
        );

        btnLogin.setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class))
        );
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        Surface surface = new Surface(surfaceTexture);

        try {
            Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.fondo1);
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(this, videoUri);
            mediaPlayer.setSurface(surface);
            mediaPlayer.setLooping(true);
            mediaPlayer.setVolume(0f, 0f); // Sin sonido
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(mp -> {
                ajustarEscala(mp.getVideoWidth(), mp.getVideoHeight());
                mp.start();
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void ajustarEscala(int videoWidth, int videoHeight) {
        int viewWidth = bgVideo.getWidth();
        int viewHeight = bgVideo.getHeight();

        float scaleX = (float) viewWidth / videoWidth;
        float scaleY = (float) viewHeight / videoHeight;
        float scale = Math.max(scaleX, scaleY); // "cover": cubre toda la pantalla

        float scaledWidth = videoWidth * scale;
        float scaledHeight = videoHeight * scale;

        float offsetX = (scaledWidth - viewWidth) / 2f;
        float offsetY = (scaledHeight - viewHeight) / 2f;

        Matrix matrix = new Matrix();
        matrix.setScale(scaledWidth / viewWidth, scaledHeight / viewHeight);
        matrix.postTranslate(-offsetX, -offsetY);
        bgVideo.setTransform(matrix);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {}

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {}

    @Override
    protected void onResume() {
        super.onResume();
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }
}