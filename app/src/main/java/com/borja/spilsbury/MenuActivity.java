package com.borja.spilsbury;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.borja.spilsbury.logica.Preferencias;
import com.borja.spilsbury.logica.Usuario;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class MenuActivity extends AppCompatActivity {

    private Bundle bundle;
    private String email, fecha;
    private Button btnReto, btnJugar, btnPerfil, btnRanking, btnOpciones, btnSalir;
    private Usuario user;
    private FirebaseFirestore db;
    private MediaPlayer mp;
    private SharedPreferences preferencias;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        db = FirebaseFirestore.getInstance();
        bundle = getIntent().getExtras();
        email = bundle.getString("email");
        fecha = obtenerFechaConFormato("yyyy-MM-dd", "GMT-1");
        recuperarDatosUsuario();
        inicializar();

        btnReto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (user.getFechaReto().equals(fecha)) {
                    showAlert();
                } else {
                    Intent i = new Intent(MenuActivity.this, GameActivity.class);
                    i.putExtra("email", email);
                    i.putExtra("juego", "online");
                    startActivity(i);
                }
            }
        });

        btnJugar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MenuActivity.this, ImageActivity.class);
                i.putExtra("email", email);
                i.putExtra("juego", "local");
                startActivity(i);
            }
        });

        btnPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MenuActivity.this, HomeActivity.class);
                i.putExtra("email", email);
                startActivity(i);

            }
        });

        btnRanking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MenuActivity.this, RankingActivity.class);
                startActivity(i);

            }
        });

        btnOpciones.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MenuActivity.this, Preferencias.class);
                i.putExtra("email", email);
                startActivity(i);
            }
        });

        btnSalir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishAffinity();
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        preferencias = PreferenceManager.getDefaultSharedPreferences(this);
    }
    @Override
    public void onResume() {
        super.onResume();
        comprobarPreferenciaInterfaz();
    }

    private void comprobarPreferenciaInterfaz() {

        if (preferencias.getString("interfaz", "0").equals("0")) {
            lanzarInterfazClaro();

        } else {
            lanzarInterfazOscuro();

        }
    }

    public void lanzarInterfazOscuro() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
    }

    public void lanzarInterfazClaro() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }

    /*private void comprobarPreferenciaMusica() {

        if (preferencias.getBoolean("musica", true)) {
            Toast.makeText(this, "Música activada en preferencias",
                    Toast.LENGTH_LONG).show();

            lanzarMelodia();

        } else {
            Toast.makeText(this, "Música no activada en preferencias",
                    Toast.LENGTH_LONG).show();
            pararMelodia();

        }
    }

    private void lanzarMelodia() {
        mp=MediaPlayer.create(this, R.raw.musicafondo);
        if (!mp.isPlaying()) {
            Toast.makeText(this, "Que suene la música",
                    Toast.LENGTH_LONG).show();
            mp.start();
        }
    }

    private void pararMelodia() {
        if(mp!=null){
            if (mp.isPlaying()) {
                Toast.makeText(this, "Que pare la música",
                        Toast.LENGTH_LONG).show();
                mp.stop();
            }
        }
    }*/

    public void inicializar() {
        btnReto = (Button) findViewById(R.id.buttonReto);
        btnJugar = (Button) findViewById(R.id.buttonJuego);
        btnPerfil = (Button) findViewById(R.id.buttonPerfil);
        btnRanking = (Button) findViewById(R.id.buttonRanking);
        btnOpciones = (Button) findViewById(R.id.buttonOpciones);
        btnSalir = (Button) findViewById(R.id.buttonCerrarApp);
    }

    @SuppressLint("SimpleDateFormat")
    public static String obtenerFechaConFormato(String formato, String zonaHoraria) {
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        SimpleDateFormat sdf;
        sdf = new SimpleDateFormat(formato);
        sdf.setTimeZone(TimeZone.getTimeZone(zonaHoraria));
        return sdf.format(date);
    }

    private void recuperarDatosUsuario() {
        db.collection("usuarios").document(email)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            user = documentSnapshot.toObject(Usuario.class);
                        }
                    }
                });
    }

    public void showAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("INFO");
        builder.setMessage("Ya has realizado el reto de hoy, vuelve mañana");
        builder.setPositiveButton("Aceptar", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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
    public void lanzarPerfil() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtra("email", email);
        startActivity(intent);
    }

    // Lanzamos la activity del Ranking online
    public void lanzarRanking() {
        Intent intent = new Intent(this, RankingActivity.class);
        startActivity(intent);
    }

    // Lanzamos las preferencias
    public void lanzarPreferencias() {
        Intent intent = new Intent(this, Preferencias.class);
        intent.putExtra("email", email);
        startActivity(intent);
    }

    // Cerramos la sesion del usuario, borramos las preferencias del usuario y volvemos a la activity de autentificación.
    public void cerrarSesion() {
        SharedPreferences sharedPrefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = sharedPrefs.edit();
        prefsEditor.clear();
        prefsEditor.apply();
        FirebaseAuth.getInstance().signOut();
        Intent i = new Intent(this, AuthActivity.class);
        startActivity(i);
    }
}