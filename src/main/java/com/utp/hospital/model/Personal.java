package com.utp.hospital.model;

import java.io.Serializable;

public class Personal implements Serializable {

    private static final long serialVersionUID = 1L;

    private int idPersonal;
    private String dni;
    private String nombre;
    private String cargo;
    private Integer idTurno;

    public Personal() {
    }

   public String getEtiqueta() {
        return nombre + " (" + dni + ")";
    }

    public int getIdPersonal() { return idPersonal; }
    public void setIdPersonal(int idPersonal) { this.idPersonal = idPersonal; }
    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getCargo() { return cargo; }
    public void setCargo(String cargo) { this.cargo = cargo; }
    public Integer getIdTurno() { return idTurno; }
    public void setIdTurno(Integer idTurno) { this.idTurno = idTurno; }
}
