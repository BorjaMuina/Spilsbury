package com.borja.spilsbury;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.borja.spilsbury.logica.Preferencias;
import com.borja.spilsbury.logica.Usuario;
import com.google.android.gms.tasks.OnSuccessListener;
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
        fecha=obtenerFechaConFormato("yyyy-MM-dd","GMT-1");
        recuperarDatosUsuario();
        inicializar();

        btnReto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(user.getFechaReto().equals(fecha)){
                    showAlert();
                }else{
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
        //comprobarPreferenciaMusica();
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

    public void inicializar(){
        btnReto=(Button) findViewById(R.id.buttonReto);
        btnJugar=(Button) findViewById(R.id.buttonJuego);
        btnPerfil=(Button) findViewById(R.id.buttonPerfil);
        btnRanking=(Button) findViewById(R.id.buttonRanking);
        btnOpciones=(Button) findViewById(R.id.buttonOpciones);
        btnSalir=(Button) findViewById(R.id.buttonCerrarApp);
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
}