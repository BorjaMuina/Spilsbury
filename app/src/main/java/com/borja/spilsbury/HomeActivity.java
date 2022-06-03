package com.borja.spilsbury;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.borja.spilsbury.logica.AudioService;
import com.borja.spilsbury.logica.Preferencias;
import com.borja.spilsbury.logica.Usuario;
import com.bumptech.glide.disklrucache.DiskLruCache;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "MiActividad";

    private Bundle bundle;
    private String email, proveedor, nombreUsuario, fechaReto;
    private int puntuacionLocal, puntuacionGlobal;
    private TextView tvEmail, tvProveedor, tvPuntuacionLocal, tvPuntuacionGlobal;
    private EditText etNombreUsuario;
    private Button btnCerrarSesion, btnModificar, btnSalir;
    private SharedPreferences sharedPrefs;
    private SharedPreferences.Editor prefsEditor;
    private Usuario user;
    private SharedPreferences preferencias;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        iniciarComponentes();

        bundle = getIntent().getExtras();
        email = bundle.getString("email");
        proveedor = bundle.getString("proveedor");

        // Recuperar datos del usuario si existe
        recuperarDatos();

        // Iniciar sesión
        cerrarSesion();

        // Modificamos datos del usuario
        modificarDatos();

        btnSalir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(HomeActivity.this, MenuActivity.class);
                i.putExtra("email", email);
                startActivity(i);
            }
        });

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

    // Iniciamos los componentes
    public void iniciarComponentes() {
        tvEmail = (TextView) findViewById(R.id.textViewEmail);
        tvProveedor = (TextView) findViewById(R.id.textViewProveedor);
        tvPuntuacionLocal = (TextView) findViewById(R.id.textViewPuntuacionLocal);
        tvPuntuacionGlobal = (TextView) findViewById(R.id.textViewPuntuacionGlobal);
        etNombreUsuario = (EditText) findViewById(R.id.editTextNombreUsuario);
        btnCerrarSesion = (Button) findViewById(R.id.buttonCerrarSesion);
        btnModificar = (Button) findViewById(R.id.buttonModificar);
        btnSalir = (Button) findViewById(R.id.buttonSalir);
    }

    // Cerramos la sesion del usuario logeado y borramos las preferencias
    public void cerrarSesion() {

        btnCerrarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sharedPrefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE);
                prefsEditor = sharedPrefs.edit();
                prefsEditor.clear();
                prefsEditor.apply();
                FirebaseAuth.getInstance().signOut();
                Intent i=new Intent(HomeActivity.this, AuthActivity.class);
                startActivity(i);
            }
        });

    }

    // Llamamos a los datos del usuario que hay en firebase, si es nuevo introducimos los datos necesarios.
    public void recuperarDatos() {

        db.collection("usuarios").document(email)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(documentSnapshot.exists()) {
                            user= documentSnapshot.toObject(Usuario.class);
                        }else{
                            nombreUsuario=email.substring(0,6);
                            puntuacionLocal=0;
                            puntuacionGlobal=0;
                            fechaReto=" ";
                            user=new Usuario(email, proveedor, nombreUsuario, puntuacionLocal, puntuacionGlobal, fechaReto);
                            guardarDatos();
                        }
                        mostrarDatos(user);
                        // Guardar preferencias del usuario logeado
                        guardarPreferencias();
                    }
                });
    }

    // Mostramos los datos que recuperamos de firebase
    public void mostrarDatos(Usuario user){
        tvEmail.setText(user.getEmail());
        tvProveedor.setText(user.getProveedor());
        etNombreUsuario.setText(user.getNombre());
        tvPuntuacionLocal.setText(String.valueOf(user.getPuntuacionLocal()));
        tvPuntuacionGlobal.setText(String.valueOf(user.getPuntuacionOnline()));
    }

    // Guardamos un documento de preferencias con el email y proveedor para saber si hay algún usuario logeado
    public void guardarPreferencias(){
        sharedPrefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE);
        prefsEditor = sharedPrefs.edit();
        prefsEditor.putString("email", user.getEmail());
        prefsEditor.putString("proveedor", user.getProveedor());
        prefsEditor.commit();
    }

    // Guardamos los datos del usuario en firebase en caso de ser un usuario nuevo
    public void guardarDatos(){
        db.collection("usuarios").document(user.getEmail())
                .set(user)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });
    }

    // Modificamos los datos del usuario en firebase, es decir el nombre de usuario por si lo cambia.
    public void modificarDatos() {

        btnModificar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nombreModificado=String.valueOf(etNombreUsuario.getText());
                user.setNombre(nombreModificado);
                db.collection("usuarios").document(user.getEmail())
                        .set(user)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(HomeActivity.this, "NOMBRE DE USUARIO GUARDADO", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(HomeActivity.this, "NO SE HA PODIDO GUARDAR EL NOMBRE DE USUARIO", Toast.LENGTH_SHORT).show();
                            }
                        });
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
            cerrarSesion2();
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
    public void cerrarSesion2() {
        SharedPreferences sharedPrefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = sharedPrefs.edit();
        prefsEditor.clear();
        prefsEditor.apply();
        FirebaseAuth.getInstance().signOut();
        Intent i = new Intent(this, AuthActivity.class);
        startActivity(i);
    }
}