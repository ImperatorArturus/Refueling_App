package com.example.refueling.extras;

import java.io.Serializable;
import java.nio.DoubleBuffer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class Repostaje implements Serializable, Comparable<Repostaje>{
    public String id_r, fecha, precio, litros;
    public int km;

    //Constructor Vacio
    public Repostaje() {
    }

    //Constructor
    public Repostaje(String id_r, String fecha, int km, String precio, String litros) {
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

    public int getKm() {
        return km;
    }

    public void setKm(int km) {
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

    @Override
    public int compareTo(Repostaje otroRepostaje) {
        /*
        if (this.fecha.isEmpty() || otroRepostaje.fecha.isEmpty()){
            return 0;
        }
        // Compara los repostajes por fecha
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault());
        LocalDate fecha1 = LocalDate.parse(this.fecha, formatter);
        LocalDate fecha2 = LocalDate.parse(otroRepostaje.fecha, formatter);

        return fecha1.compareTo(fecha2);

         */
        try{
            double km1 = Double.valueOf(this.km);
            double km2 = Double.valueOf(otroRepostaje.km);

            km1 = Math.round(km1 * 100.0) / 100.0;
            km2 = Math.round(km2 * 100.0) / 100.0;
            System.out.println(Double.compare(km1, km2));

            return Double.compare(km1, km2);
        }catch (NumberFormatException e){
            e.printStackTrace();
            return 0;
        }
    }

}
