package com.borja.spilsbury;

import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.borja.spilsbury.logica.AudioService;
import com.borja.spilsbury.logica.Preferencias;
import com.google.android.gms.auth.api.identity.BeginSignInResult;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class AuthActivity extends AppCompatActivity {

    public static final int REQUEST_INTERNET = 1000;
    private FirebaseAnalytics mFirebaseAnalytics;
    private Button btnRegistrar, btnAcceder, btnGoogle;
    private EditText etEmail, etContraseña;
    private String email, contraseña, proveedor;
    private SharedPreferences sharedPrefs;
    private static final int REQ_ONE_TAP = 2;
    private SharedPreferences preferencias;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        iniciarComponentes();
        inicioSesion();
        session();
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

    public void iniciarComponentes() {
        btnRegistrar = (Button) findViewById(R.id.buttonRegistrar);
        btnAcceder = (Button) findViewById(R.id.buttonAcceder);
        btnGoogle = (Button) findViewById(R.id.buttonGoogle);
        etEmail = (EditText) findViewById(R.id.editTextEmail);
        etContraseña = (EditText) findViewById(R.id.editTextPassword);
    }

    // Comprobamos si hay una sesion iniciada, si tenemos un usuario conectado vamos al menu directamente
    public void session() {
        sharedPrefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE);
        email = sharedPrefs.getString("email", null);
        proveedor = sharedPrefs.getString("proveedor", null);

        if (email != null && proveedor != null) {
            showMenu(email);
        }
    }

    // Esperamos a que el usuario elija el tipo de sesion
    public void inicioSesion() {

        btnRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = etEmail.getText().toString();
                contraseña = etContraseña.getText().toString();
                if (!email.isEmpty() && !contraseña.isEmpty()) {
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, contraseña).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                showHome(email, ProviderType.CORREO_ELECTRÓNICO);
                            } else {
                                showAlert();
                            }
                        }
                    });
                }
            }
        });

        btnAcceder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = etEmail.getText().toString();
                contraseña = etContraseña.getText().toString();
                if (!email.isEmpty() && !contraseña.isEmpty()) {
                    FirebaseAuth.getInstance().signInWithEmailAndPassword(email, contraseña).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                showHome(email, ProviderType.CORREO_ELECTRÓNICO);
                            } else {
                                showAlert();
                            }
                        }
                    });
                }
            }
        });

        btnGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();
                GoogleSignInClient gsc = GoogleSignIn.getClient(AuthActivity.this, gso);
                gsc.signOut();

                startActivityForResult(gsc.getSignInIntent(), REQ_ONE_TAP);

            }
        });
    }

    // Alerta de error
    public void showAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Error");
        builder.setMessage("El Email o la contraseña no son correctos");
        builder.setPositiveButton("Aceptar", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Acceso concedido, accedemos al perfil del usuario
    public void showHome(String email, ProviderType proveedor) {
        Intent i = new Intent(AuthActivity.this, HomeActivity.class);
        i.putExtra("email", email);
        i.putExtra("proveedor", proveedor.toString());
        startActivity(i);

    }

    // Mostramos la activity menu
    public void showMenu(String email) {
        Intent i = new Intent(AuthActivity.this, MenuActivity.class);
        i.putExtra("email", email);
        startActivity(i);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_ONE_TAP:
                try {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    GoogleSignInAccount account = task.getResult(ApiException.class);

                    if (account != null) {
                        AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                        FirebaseAuth.getInstance().signInWithCredential(firebaseCredential)
                                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            showHome(task.getResult().getUser().getEmail(), ProviderType.GOOGLE);
                                        } else {
                                            showAlert();
                                        }
                                    }
                                });
                    }
                } catch (ApiException e) {
                    showAlert();
                }
                break;
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == event.KEYCODE_BACK) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("¿Quieres salir de SpilsBury?")
                    .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finishAffinity();
                        }
                    })
                    .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            builder.show();
        }

        return super.onKeyDown(keyCode, event);
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
        alertAccesoDenegado();
    }

    // Lanzamos la activity del Ranking online
    public void lanzarRanking() {
        alertAccesoDenegado();
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

    public void alertAccesoDenegado() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Error");
        builder.setMessage("Debes estar logeado para acceder");
        builder.setPositiveButton("Aceptar", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}


