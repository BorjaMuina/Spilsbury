package com.borja.spilsbury.logica;

public class Usuario {

    private String email, proveedor, nombre, fechaReto;
    private int puntuacionLocal, puntuacionOnline;

    public Usuario(String email, String proveedor, String nombre, int puntuacionLocal, int puntuacionOnline, String fechaReto) {
        this.email = email;
        this.proveedor = proveedor;
        this.nombre = nombre;
        this.puntuacionLocal=puntuacionLocal;
        this.puntuacionOnline =puntuacionOnline;
        this.fechaReto=fechaReto;
    }

    public Usuario(String nombre, int puntuacionOnline) {
        this.nombre = nombre;
        this.puntuacionOnline=puntuacionOnline;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProveedor() {
        return proveedor;
    }

    public void setProveedor(String proveedor) {
        this.proveedor = proveedor;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getPuntuacionLocal() {
        return puntuacionLocal;
    }

    public void setPuntuacionLocal(int puntuacionLocal) {
        this.puntuacionLocal = puntuacionLocal;
    }

    public int getPuntuacionOnline() {
        return puntuacionOnline;
    }

    public void setPuntuacionOnline(int puntuacionOnline) {
        this.puntuacionOnline = puntuacionOnline;
    }

    public String getFechaReto() {
        return fechaReto;
    }

    public void setFechaReto(String fechaReto) {
        this.fechaReto = fechaReto;
    }
}
