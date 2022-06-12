package com.borja.spilsbury.logica;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.ArrayList;

public class ImagenAdapter extends BaseAdapter {
    private ArrayList<ImageView> fragmentos = null;
    private int ancho, alto;

    public ImagenAdapter(ArrayList<ImageView> buttons, int columnWidth, int columnHeight) {
        fragmentos = buttons;
        ancho = columnWidth;
        alto = columnHeight;
    }

    @Override
    public int getCount() {
        return fragmentos.size();
    }

    @Override
    public Object getItem(int position) {return fragmentos.get(position);}

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView fragmento;

        if (convertView == null) {
            fragmento = fragmentos.get(position);
        } else {
            fragmento = (ImageView) convertView;
        }

        android.widget.AbsListView.LayoutParams params =
                new android.widget.AbsListView.LayoutParams(ancho, alto);
        fragmento.setLayoutParams(params);

        return fragmento;
    }
}
