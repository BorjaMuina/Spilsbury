package com.borja.spilsbury;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;

public class SplashActivity extends AppCompatActivity {

    private SharedPreferences preferencias;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
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
    }

    private void comprobarPreferenciaInterfaz() {

        if (preferencias.getString("interfaz", "0").equals("0")) {
            lanzarInterfazClaro();

        } else {
            lanzarInterfazOscuro();

        }
    }

    public void lanzarInterfazOscuro(){
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
    }

    public void lanzarInterfazClaro(){
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }
}
