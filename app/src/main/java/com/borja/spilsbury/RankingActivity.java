package com.borja.spilsbury;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.borja.spilsbury.logica.RankingAdapter;
import com.borja.spilsbury.logica.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking);
        listView=(ListView) findViewById(R.id.listView);
        recuperarDatosUsuario();

    }

    public synchronized void crearAdaptador(){
        RankingAdapter adaptador=new RankingAdapter(this, R.layout.ranking, listaUsuarios);
        listView.setAdapter(adaptador);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
    }

    public synchronized void recuperarDatosUsuario() {

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
}