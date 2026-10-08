package com.utp.hospital.model;

import java.io.Serializable;
import java.time.LocalDate;


public class FiltroAsistencia implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer idPersonal; 
    private String texto;        
    private String cargo;       
    private String turno;    
    private String estado;  
    private String permiso; 
    private String tipoPermiso; 
    private LocalDate desde;
    private LocalDate hasta;

    public Integer getIdPersonal() { return idPersonal; }
    public void setIdPersonal(Integer idPersonal) { this.idPersonal = idPersonal; }
    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }
    public String getCargo() { return cargo; }
    public void setCargo(String cargo) { this.cargo = cargo; }
    public String getTurno() { return turno; }
    public void setTurno(String turno) { this.turno = turno; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getPermiso() { return permiso; }
    public void setPermiso(String permiso) { this.permiso = permiso; }
    public String getTipoPermiso() { return tipoPermiso; }
    public void setTipoPermiso(String tipoPermiso) { this.tipoPermiso = tipoPermiso; }
    public LocalDate getDesde() { return desde; }
    public void setDesde(LocalDate desde) { this.desde = desde; }
    public LocalDate getHasta() { return hasta; }
    public void setHasta(LocalDate hasta) { this.hasta = hasta; }
}
