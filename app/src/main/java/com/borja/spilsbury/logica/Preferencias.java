package com.borja.spilsbury.logica;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.borja.spilsbury.AuthActivity;
import com.borja.spilsbury.HomeActivity;
import com.borja.spilsbury.R;
import com.borja.spilsbury.RankingActivity;
import com.google.firebase.auth.FirebaseAuth;

public class Preferencias extends AppCompatActivity {

    private Bundle bundle;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportFragmentManager().beginTransaction().replace(android.R.id.content, new PreferenciasFragment()).commit();

        bundle = getIntent().getExtras();
        email = bundle.getString("email");

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.preferencias) {
            lanzarPreferencias();
            return true;
        }
        if (id == R.id.pefil) {
            lanzarPerfil();
            return true;
        }

        if (id == R.id.ranking) {
            lanzarRanking();
            return true;
        }

        if (id == R.id.cerrarSesion) {
            cerrarSesion();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Lanzamos la activity del perfil del usuario
    public void lanzarPerfil(){
        Intent intent=new Intent(this, HomeActivity.class);
        intent.putExtra("email", email);
        startActivity(intent);
    }

    // Lanzamos la activity del Ranking online
    public void lanzarRanking(){
        Intent intent=new Intent(this, RankingActivity.class);
        startActivity(intent);
    }

    // Lanzamos las preferencias
    public void lanzarPreferencias(){
        Intent intent=new Intent(this,Preferencias.class);
        intent.putExtra("email", email);
        startActivity(intent);
    }

    // Cerramos la sesion del usuario, borramos las preferencias del usuario y volvemos a la activity de autentificación.
    public void cerrarSesion(){
        SharedPreferences sharedPrefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = sharedPrefs.edit();
        prefsEditor.clear();
        prefsEditor.apply();
        FirebaseAuth.getInstance().signOut();
        Intent i=new Intent(this, AuthActivity.class);
        startActivity(i);
    }


}
