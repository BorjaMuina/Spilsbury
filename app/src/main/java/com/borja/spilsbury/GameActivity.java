package com.borja.spilsbury;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.GridView;
import android.widget.ImageView;
import com.borja.spilsbury.logica.AudioService;
import com.borja.spilsbury.logica.ImagenAdapter;
import com.borja.spilsbury.logica.Pieza;
import com.borja.spilsbury.logica.Preferencias;
import com.borja.spilsbury.logica.Usuario;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.TimeZone;

public class GameActivity extends AppCompatActivity {

    public static final String KEY_IMAGEN = "IMAGEN";
    private static final String TAG = "MiActividad";
    private ImageView imagenPuzle;
    private Bitmap imagenMap, bitmap;
    private Uri imagenUri;
    private GridView tablero;
    private Usuario user;
    private Bundle bundle;
    private String email, tipoJuego, dispositivo, idImajen, url, fecha;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static int dimension = 3;
    private static int anchoColum, altoColum;
    private ArrayList<Pieza> piezas;
    private ArrayList<Pieza> piezasDesordenadas;
    private Animation animacion;
    private int puntosActuales;
    private Long inicio;
    private Long fin;
    private int posicionA = -1, posicionB = -1;
    private SharedPreferences preferencias;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        bundle = getIntent().getExtras();
        email = bundle.getString("email");
        tipoJuego = bundle.getString("juego");
        dispositivo = bundle.getString("dispositivo");
        inicializar();

    }

    //Cargamos las preferencias del usuario en cuanto a las opciones
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

    // Activamos modo oscuro
    public void lanzarInterfazOscuro() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
    }

    // Activamos modo claro
    public void lanzarInterfazClaro() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }

    // Comprobamos si la música esta activada o no
    private void comprobarPreferenciaMusica() {
        if (preferencias.getBoolean("musica", true)) {
            lanzarMelodia();
        } else {
            pararMelodia();
        }
    }

    // Iniciamos música
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

    public void inicializar() {
        imagenPuzle = findViewById(R.id.imagenPuzle);
        tablero = (GridView) findViewById(R.id.tablaPuzle);
        animacion = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.animacion);
        puntosActuales = 0;
        recuperarDatosUsuario();
        setImage();
    }

    // Recuperamos los datos del usuario logeado
    public void recuperarDatosUsuario() {
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


    // Comprobamos que modo de juego se escogió y cargamos la imagen
    public void setImage() {
        switch (tipoJuego) {
            case "local":
                if (dispositivo.equals("galeria")) {
                    Intent intent = getIntent();
                    imagenUri = intent.getExtras().getBundle(KEY_IMAGEN).getParcelable(KEY_IMAGEN);
                    imagenPuzle.setImageURI(imagenUri);
                } else {
                    imagenMap = getIntent().getParcelableExtra(KEY_IMAGEN);
                    imagenPuzle.setImageBitmap(imagenMap);
                }
                inicializarTablero();
                break;
            case "online":
                fecha = obtenerFechaConFormato("yyyy-MM-dd", "GMT-1");
                idImajen = fecha.substring(8, 10);
                cargarImagen();
                break;
        }

    }

    // Obtenemos la fecha actual
    @SuppressLint("SimpleDateFormat")
    public static String obtenerFechaConFormato(String formato, String zonaHoraria) {
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        SimpleDateFormat sdf;
        sdf = new SimpleDateFormat(formato);
        sdf.setTimeZone(TimeZone.getTimeZone(zonaHoraria));
        return sdf.format(date);
    }

    // En el modo online cargamos la imagen de la base de datos y la obtenemos con glide
    public void cargarImagen() {
        db.collection("Imajenes").document(idImajen)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            url = documentSnapshot.getString("URL");
                        }
                        Glide.with(GameActivity.this)
                                .asBitmap()
                                .load(url)
                                .into(new CustomTarget<Bitmap>() {
                                    @Override
                                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                        imagenPuzle.setImageBitmap(resource);
                                        inicializarTablero();
                                    }
                                    @Override
                                    public void onLoadCleared(@Nullable Drawable placeholder) {
                                    }
                                });
                    }
                });
    }

    // Mostramos la imagen y ejecutamos lo métodos para crear el puzzle
    public void inicializarTablero() {
        piezas = new ArrayList<>();
        piezasDesordenadas = new ArrayList<>();
        tablero.setVisibility(View.INVISIBLE);
        imagenPuzle.setVisibility(View.VISIBLE);
        tablero.removeAllViewsInLayout();
        partirImagen();
        desordenar();
        pintar();

    }

    // Dividimos la imagen en piezas y las guardamos en un ArrayList
    public void partirImagen() {
        piezas = new ArrayList<>();
        int ancho = 0;
        int alto = 0;
        int posicion = 0;

        //preparamos la imagen para ser cortada
        BitmapDrawable drawable = (BitmapDrawable) imagenPuzle.getDrawable();
        bitmap = drawable.getBitmap();

        ancho = bitmap.getWidth() / dimension;
        alto = bitmap.getHeight() / dimension;

        //partimos la imagen en piezas
        int coordY = 0;
        for (int f = 0; f < dimension; f++) {
            int coordX = 0;
            for (int c = 0; c < dimension; c++) {
                piezas.add(new Pieza(coordX, coordY, ancho, alto, posicion, 0, Bitmap.createBitmap(bitmap, coordX, coordY, ancho, alto)));
                coordX += ancho;
                posicion++;
            }
            coordY += alto;
        }
    }

    // Desordenamos el ArrayList de piezas
    public void desordenar() {
        ArrayList<Pieza> copiaPiezas = new ArrayList<>(piezas);
        Random random = new Random();
        Pieza actual = null;
        int posicionActual = 0;
        while (copiaPiezas.size() > 0) {
            int posicion = random.nextInt(copiaPiezas.size());
            actual = copiaPiezas.get(posicion);
            actual.setPosicionActual(posicionActual);
            piezasDesordenadas.add(actual);
            copiaPiezas.remove(actual);
            posicionActual++;
        }
    }

    // Tiempo de creación del puzle
    private void pintar() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                estableceDimension();
            }
        }, 2000);

    }

    // Guardamos el tiempo inicial y definimos las medidas del tablero
    private void estableceDimension() {
        inicio = System.currentTimeMillis();
        int displayWidth = tablero.getMeasuredWidth();
        int displayHeight = tablero.getMeasuredHeight();

        anchoColum = displayWidth / dimension;
        altoColum = displayHeight / dimension;

        pintarPuzle();
    }


    // Pasamos los datos al ImagenAdapter y lo cargamos
    private void pintarPuzle() {
        ArrayList<ImageView> fragmentos = new ArrayList<>();
        for (Pieza p : piezasDesordenadas) {
            ImageView fragmento = new ImageView(this);
            BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), p.getImagen());
            fragmento.setBackgroundDrawable(bitmapDrawable);
            fragmentos.add(fragmento);
        }
        imagenPuzle.setVisibility(View.INVISIBLE);
        tablero.setNumColumns(dimension);
        tablero.removeAllViewsInLayout();
        tablero.setAdapter(new ImagenAdapter(fragmentos, anchoColum, altoColum));
        tablero.setOnItemClickListener((parent, view, position, id) -> {
            intercambia(position);
            view.startAnimation(animacion);
        });
        tablero.setVisibility(View.VISIBLE);
    }

    // Vamos asignando las nuevas posiciones a las piezas según se mueven en el tablero
    private void intercambia(int posicion) {
        //reproductor.start();
        Pieza piezaA, piezaB;
        if (posicionA == -1 || posicionA == posicion) {
            posicionA = posicion;
        } else {
            posicionB = posicion;
            piezaA = piezasDesordenadas.get(posicionA);
            piezaB = piezasDesordenadas.get(posicionB);
            piezaA.setPosicionActual(posicionB);
            piezaB.setPosicionActual(posicionA);
            piezasDesordenadas.set(posicionA, piezaB);
            piezasDesordenadas.set(posicionB, piezaA);
            posicionA = -1;
            posicionB = -1;
            pintarPuzle();
        }
        // Comprobamos se está resuelto el puzle
        if (isSolucion()) {
            resolver();
        }
    }

    // Vamos comprobando las posiciones de las piezas del puzzle
    private boolean isSolucion() {
        for (Pieza p : piezasDesordenadas) {
            if (p.getPosicionActual() != p.getPosicionFinal())
                return false;
        }
        return true;
    }

    // Asignar los puntos obtenidos al usuario y mostrar la alerta final
    private void resolver() {
        fin = System.currentTimeMillis();
        int puntos = 1000000 / (int) (fin - inicio);
        // Comprobamos la dificultad del puzle
        if(dimension!=3){
            int bonus=(dimension-3)+1;
            puntos=puntos*bonus;
        }
        puntosActuales += puntos;
        // Mostramos mensage final según el juego en el que estemos
        switch (tipoJuego) {
            case "local":
                alertaFinalLocal();
                break;
            case "online":
                alertaFinalOnline();
                break;
        }
        registrarPuntosFirebase(puntos);
    }

    // Alerta final del juego local con los puntos conseguidos y preguntando si queremos seguir jugando
    public void alertaFinalLocal() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("PUNTUACIÓN: " + puntosActuales);
        builder.setMessage("¿Quieres seguir jugando? Se incrementará la dificultad")
                .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dimension++;
                        showImage();
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showHome();
                    }
                });
        builder.show();
    }

    // Alerta final del juego online con los puntos conseguidos
    public void alertaFinalOnline() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("PUNTUACIÓN: " + puntosActuales);
        builder.setMessage("Ten paciencia, mañana tendrás un nuevo reto")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showRanking();
                    }
                });
        builder.show();
    }

    // Mostramos menu
    private void showHome() {
        Intent i = new Intent(this, HomeActivity.class);
        i.putExtra("email", email);
        startActivity(i);
        finish();
    }

    // Volvemos a escoger el tipo de carga de imagen en el modo local
    private void showImage() {
        Intent i = new Intent(this, ImageActivity.class);
        i.putExtra("email", email);
        startActivity(i);
    }

    //Mostramos Ranking
    private void showRanking() {
        Intent i = new Intent(this, RankingActivity.class);
        i.putExtra("email", email);
        startActivity(i);
        finish();
    }


    // Añadimos la nueva puntuación al usuario en la base de datos de firebase
    private void registrarPuntosFirebase(int puntos) {

        switch (tipoJuego) {
            case "local":
                puntosActuales = user.getPuntuacionLocal() + puntos;
                user.setPuntuacionLocal(puntosActuales);
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
                break;
            case "online":
                puntosActuales = user.getPuntuacionOnline() + puntos;
                user.setPuntuacionOnline(puntosActuales);
                user.setFechaReto(fecha);
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
                break;
        }
    }


    // Controlamos el boton por defecto de ir atrás de android
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == event.KEYCODE_BACK) {
            if (tipoJuego.equals("local")) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("¿Quieres salir del Puzzle? La dificultad se reiniciara")
                        .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dimension = 3;
                                Intent i = new Intent(GameActivity.this, MenuActivity.class);
                                i.putExtra("email", email);
                                startActivity(i);
                                finish();
                            }
                        })
                        .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                builder.show();
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("¿Quieres salir del Puzzle? No se guardará el reto")
                        .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent i = new Intent(GameActivity.this, MenuActivity.class);
                                i.putExtra("email", email);
                                startActivity(i);
                                finish();
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
        }
        return super.onKeyDown(keyCode, event);
    }

    // Controlamos el boton por defecto de ir atrás de android 12 y algunas versiones
    @Override
    public void onBackPressed() {
        if (tipoJuego.equals("local")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("¿Quieres salir del Puzzle? La dificultad se reiniciara")
                    .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dimension = 3;
                            Intent i = new Intent(GameActivity.this, MenuActivity.class);
                            i.putExtra("email", email);
                            startActivity(i);
                            finish();
                        }
                    })
                    .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            builder.show();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("¿Quieres salir del Puzzle? No se guardará el reto")
                    .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent i = new Intent(GameActivity.this, MenuActivity.class);
                            i.putExtra("email", email);
                            startActivity(i);
                            finish();
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

    // Cerramos la sesión del usuario, borramos las preferencias del usuario y volvemos a la activity de autentificación.
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