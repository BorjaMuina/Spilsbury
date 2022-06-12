package com.borja.spilsbury;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;

import com.borja.spilsbury.logica.AudioService;

public class SplashActivity extends AppCompatActivity {

    private SharedPreferences preferencias;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Ejecutamos un nuevo hilo runnable con un tiempo de espera para que cargue la pantalla
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent= new
                        Intent(SplashActivity.this, AuthActivity.class);
                startActivity(intent);
                finish();
            }
        },2000);
    }

    @Override
    public void onStart() {
        super.onStart();
        preferencias = PreferenceManager.getDefaultSharedPreferences(this);
        comprobarPreferenciaInterfaz();
        comprobarPreferenciaMusica();
    }

    // Comprobamos las preferencias guaradas del usuario en cuanto al interfaz
    private void comprobarPreferenciaInterfaz() {
        if (preferencias.getString("interfaz", "0").equals("0")) {
            lanzarInterfazClaro();
        } else {
            lanzarInterfazOscuro();
        }
    }

    // Activamos el modo oscuro
    public void lanzarInterfazOscuro(){
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
    }

    // Activamos el modo claro
    public void lanzarInterfazClaro(){
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }

    // Comprobamos si la musica esta activada o no en las preferencias
    private void comprobarPreferenciaMusica() {
        if (preferencias.getBoolean("musica", true)) {
            lanzarMelodia();
        } else {
            pararMelodia();
        }
    }

    // Activamos música
    private void lanzarMelodia() {
        Intent i = new Intent(this, AudioService.class);
        i.putExtra("action", AudioService.START);
        startService(i);
    }

    // Paramos música
    private void pararMelodia() {
        Intent i = new Intent(this, AudioService.class);
        i.putExtra("action", AudioService.PAUSE);
        startService(i);
    }
}
