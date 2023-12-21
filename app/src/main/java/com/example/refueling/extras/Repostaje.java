package com.example.refueling.extras;

import java.io.Serializable;

public class Repostaje implements Serializable {
    public String id_r, km, fecha, precio, litros;

    //Constructor Vacio
    public Repostaje() {
    }

    //Constructor
    public Repostaje(String id_r, String fecha, String km, String precio, String litros) {
        this.id_r = id_r;
        this.fecha = fecha;
        this.km = km;
        this.precio = precio;
        this.litros = litros;
    }

    public String getId_r() {
        return id_r;
    }

    public void setId_r(String id_r) {
        this.id_r = id_r;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getKm() {
        return km;
    }

    public void setKm(String km) {
        this.km = km;
    }

    public String getPrecio() {
        return precio;
    }

    public void setPrecio(String precio) {
        this.precio = precio;
    }

    public String getLitros() {
        return litros;
    }

    public void setLitros(String litros) {
        this.litros = litros;
    }

}
