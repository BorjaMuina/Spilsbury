package com.borja.spilsbury;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
}