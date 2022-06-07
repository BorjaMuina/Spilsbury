package com.borja.spilsbury;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.borja.spilsbury.logica.AudioService;
import com.borja.spilsbury.logica.Preferencias;
import com.borja.spilsbury.logica.RankingAdapter;
import com.borja.spilsbury.logica.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.core.OrderBy;

import java.util.ArrayList;


public class RankingActivity extends AppCompatActivity {

    private final static String TAG="1";
    private ListView listView;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Usuario user;
    private ArrayList<Usuario> listaUsuarios=new ArrayList<Usuario>();
    private SharedPreferences preferencias;
    private Bundle bundle;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking);
        bundle = getIntent().getExtras();
        email = bundle.getString("email");
        listView=(ListView) findViewById(R.id.listView);
        recuperarDatosUsuario();

    }

    //Cargamos las preferencias del usuario en cuento a las opciones
    @Override
    public void onStart() {
        super.onStart();
        preferencias = PreferenceManager.getDefaultSharedPreferences(this);
    }

    // Paramos la musica
    @Override
    public void onPause() {
        super.onPause();
        Intent i = new Intent(this, AudioService.class);
        i.putExtra("action", AudioService.PAUSE);
        startService(i);
    }

    // Comprobamos la preferencias
    @Override
    public void onResume() {
        super.onResume();
        comprobarPreferenciaInterfaz();
        comprobarPreferenciaMusica();

    }

    // Comprobamos que tema esta marcado
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

    // Comprobamos si la musica esta activada o no
    private void comprobarPreferenciaMusica() {

        if (preferencias.getBoolean("musica", true)) {
            lanzarMelodia();

        } else {
            pararMelodia();

        }
    }

    private void lanzarMelodia() {
        Intent i = new Intent(this, AudioService.class);
        i.putExtra("action", AudioService.START);
        startService(i);
    }

    private void pararMelodia() {
        Intent i = new Intent(this, AudioService.class);
        i.putExtra("action", AudioService.PAUSE);
        startService(i);
    }

    // Pasamos los datos y mostramos el adaptador creado para el ranking
    public void crearAdaptador(){
        RankingAdapter adaptador=new RankingAdapter(this, R.layout.ranking, listaUsuarios);
        listView.setAdapter(adaptador);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
    }

    // Recuperamos los datos del usuario logeado
    public void recuperarDatosUsuario() {

        db.collection("usuarios").orderBy("puntuacionOnline", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                user=document.toObject(Usuario.class);
                                listaUsuarios.add(new Usuario(user.getNombre(),user.getPuntuacionOnline()));
                            }
                            crearAdaptador();
                        } else {
                            Toast.makeText(RankingActivity.this, "No se ha podido cargar el ranking de usuarios", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    // Insertamos la barra de menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    // Comprobamos que item se ha seleccionado
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
        intent.putExtra("email", email);
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