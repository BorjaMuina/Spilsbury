package com.borja.spilsbury.logica;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.borja.spilsbury.R;

import java.util.ArrayList;

public class RankingAdapter extends BaseAdapter {

    private Context contexto;
    private int layout;
    private ArrayList<Usuario> listaUsuarios;

    public RankingAdapter(Context contexto, int layout, ArrayList<Usuario> listaUsuarios) {
        this.contexto = contexto;
        this.layout = layout;
        this.listaUsuarios = listaUsuarios;
    }


    @Override
    public int getCount() {
        return this.listaUsuarios.size();
    }

    @Override
    public Object getItem(int position) {
        return this.listaUsuarios.get(position);
    }

    @Override
    public long getItemId(int id) {
        return id;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v=convertView;
        LayoutInflater layoutInflater=LayoutInflater.from(this.contexto);
        v=layoutInflater.inflate(R.layout.ranking, null);

        Usuario user= listaUsuarios.get(position);

        TextView nUsuario= (TextView)v.findViewById(R.id.textViewNombreRanking);
        nUsuario.setText(user.getNombre());

        TextView puntuacionRanking=(TextView)v.findViewById(R.id.textViewPuntuacionRanking);
        puntuacionRanking.setText(String.valueOf(user.getPuntuacionOnline()));

        return v;
    }
}
