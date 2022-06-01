package com.borja.spilsbury;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.borja.spilsbury.logica.ImagenAdapter;
import com.borja.spilsbury.logica.Pieza;
import com.borja.spilsbury.logica.Usuario;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;

public class GameActivity extends AppCompatActivity {

    public static final String KEY_IMAGEN = "IMAGEN";
    public static final String KEY_BITMAP = "BITMAP";
    private static final String TAG = "MiActividad";
    private ImageView imagenPuzle;
    private Bitmap imagenMap, bitmap;
    private Uri imagenUri;
    private GridView tablero;
    private Usuario user;
    private Bundle bundle;
    private String email, tipoJuego,dispositivo, idImajen, url, fecha;
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

    public void inicializar() {
        imagenPuzle = findViewById(R.id.imagenPuzle);
        tablero = (GridView) findViewById(R.id.tablaPuzle);
        animacion = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.animacion);
        puntosActuales = 0;
        recuperarDatosUsuario();
        setImage();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(GameActivity.this, "Realizando el Puzzle", Toast.LENGTH_SHORT).show();
                inicializarTablero();
            }
        },2000);
        /*Handler handler = new Handler();
        handler.postDelayed(() -> {

        }, 2000);*/
    }

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
                break;
            case "online":
                fecha = obtenerFechaConFormato("yyyy-MM-dd", "GMT-1");
                idImajen = fecha.substring(8, 10);
                cargarImagen();
                break;
        }

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
                                    }

                                    @Override
                                    public void onLoadCleared(@Nullable Drawable placeholder) {
                                    }
                                });

                    }
                });

    }

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

    private void pintar() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                estableceDimension();
            }
        },2000);

    }

    private void estableceDimension() {
        inicio = System.currentTimeMillis();
        int displayWidth = tablero.getMeasuredWidth();
        int displayHeight = tablero.getMeasuredHeight();

        anchoColum = displayWidth / dimension;
        altoColum = displayHeight / dimension;

        pintarPuzle();
    }

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
        if (isSolucion()) {
            resolver();
        }
    }

    private boolean isSolucion() {
        for (Pieza p : piezasDesordenadas) {
            if (p.getPosicionActual() != p.getPosicionFinal())
                return false;
        }
        return true;
    }

    private void resolver() {
        fin = System.currentTimeMillis();
        int puntos =  1000/(int)(fin - inicio);
        puntosActuales += puntos;
        Toast.makeText(this, "¡¡Puzle completado!!\n Puntuación: " + puntos, Toast.LENGTH_LONG).show();
        dimension++;

        registrarPuntosFirebase(puntos);
        showHome();
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

    private void showHome() {
        Intent i = new Intent(this, HomeActivity.class);
        i.putExtra("email", email);
        startActivity(i);
    }

}