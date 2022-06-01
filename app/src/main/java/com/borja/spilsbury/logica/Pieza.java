package com.borja.spilsbury.logica;

import android.graphics.Bitmap;

public class Pieza {

    private int coordenadaX;
    private int coordenadaY;
    private int anchoPieza;
    private int altoPieza;
    private int posicionFinal;
    private int posicionActual;
    private Bitmap imagen;

    public Pieza(int coordenadaX, int coordenadaY, int anchoPieza, int altoPieza, int posicionFinal, int posicionActual, Bitmap imagen) {
        this.coordenadaX = coordenadaX;
        this.coordenadaY = coordenadaY;
        this.anchoPieza = anchoPieza;
        this.altoPieza = altoPieza;
        this.posicionFinal = posicionFinal;
        this.posicionActual = posicionActual;
        this.imagen = imagen;
    }

    public int getCoordenadaX() {
        return coordenadaX;
    }

    public void setCoordenadaX(int coordenadaX) {
        this.coordenadaX = coordenadaX;
    }

    public int getCoordenadaY() {
        return coordenadaY;
    }

    public void setCoordenadaY(int coordenadaY) {
        this.coordenadaY = coordenadaY;
    }

    public int getAnchoPieza() {
        return anchoPieza;
    }

    public void setAnchoPieza(int anchoPieza) {
        this.anchoPieza = anchoPieza;
    }

    public int getAltoPieza() {
        return altoPieza;
    }

    public void setAltoPieza(int altoPieza) {
        this.altoPieza = altoPieza;
    }

    public int getPosicionFinal() {
        return posicionFinal;
    }

    public void setPosicionFinal(int posicionFinal) {
        this.posicionFinal = posicionFinal;
    }

    public int getPosicionActual() {
        return posicionActual;
    }

    public void setPosicionActual(int posicionActual) {
        this.posicionActual = posicionActual;
    }

    public Bitmap getImagen() {
        return imagen;
    }

    public void setImagen(Bitmap imagen) {
        this.imagen = imagen;
    }
}