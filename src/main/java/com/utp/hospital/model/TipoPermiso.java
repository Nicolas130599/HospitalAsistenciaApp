package com.utp.hospital.model;

import java.io.Serializable;

public class TipoPermiso implements Serializable {

    private static final long serialVersionUID = 1L;

    private int idTipo;
    private String nombre;

    public TipoPermiso() {
    }

    public TipoPermiso(int idTipo, String nombre) {
        this.idTipo = idTipo;
        this.nombre = nombre;
    }

    public int getIdTipo() { return idTipo; }
    public void setIdTipo(int idTipo) { this.idTipo = idTipo; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}
