package com.borja.spilsbury;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.FileDescriptor;
import java.io.IOException;

public class ImageActivity extends AppCompatActivity {

    public static final int REQUEST_READ_EXTERNAL_CONTENT = 1000;
    public static final int REQUEST_CAMARA = 1;
    public static final int REQUEST_GALERIA= 2;
    private ImageButton buttonGaleria, buttonCamera;
    private Bundle bundle;
    private String email;
    private Bitmap imagenGuardada;
    private Uri miPath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        bundle = getIntent().getExtras();
        email = bundle.getString("email");
        inicializar();
        setup();

    }

    public void inicializar() {
        buttonCamera = (ImageButton) findViewById(R.id.imageButtonCamera);
        buttonGaleria = (ImageButton) findViewById(R.id.imageButtonGaleria);
    }

    public void setup() {

        buttonCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cargarCamara();
            }
        });

        buttonGaleria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cargarGaleria();
            }
        });

    }

    public void cargarCamara() {
        if (comprobarPermisos()) {
            Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (i.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(i, REQUEST_CAMARA);
            }
        }

    }

    private void cargarGaleria() {
        if (comprobarPermisos()){
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQUEST_GALERIA);
        }

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CAMARA && resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            imagenGuardada = (Bitmap) bundle.get("data");
            Intent intent = new Intent(ImageActivity.this, GameActivity.class);
            intent.putExtra(GameActivity.KEY_IMAGEN, imagenGuardada);
            intent.putExtra("email", email);
            intent.putExtra("juego", "local");
            intent.putExtra("dispositivo", "camara");
            startActivity(intent);
        } else if((requestCode == REQUEST_GALERIA && resultCode == RESULT_OK)){
            miPath=data.getData();
            assert miPath!=null;
            Intent intent = new Intent(ImageActivity.this, GameActivity.class);
            Bundle bundle = new Bundle();
            bundle.putParcelable(GameActivity.KEY_IMAGEN, miPath);
            intent.putExtra(GameActivity.KEY_IMAGEN, bundle);
            intent.putExtra("email", email);
            intent.putExtra("juego", "local");
            intent.putExtra("dispositivo", "galeria");
            startActivity(intent);
        }

    }

    @RequiresApi(api= Build.VERSION_CODES.M)
    public boolean comprobarPermisos() {
            if (checkSelfPermission(
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(ImageActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(ImageActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(
                        Manifest.permission.READ_EXTERNAL_STORAGE) && shouldShowRequestPermissionRationale(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) && shouldShowRequestPermissionRationale(
                        Manifest.permission.CAMERA)) {
                    Toast.makeText(this, "El permiso de acceso a datos del teléfono es necesario para acceder a las imagenes de la galería", Toast.LENGTH_SHORT).show();

                }

                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
                        REQUEST_READ_EXTERNAL_CONTENT);

            }else{
                return true;
            }

        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_READ_EXTERNAL_CONTENT) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permisos concedidos.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Es necesario el permiso de acceso a datos del teléfono para jugar", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}

