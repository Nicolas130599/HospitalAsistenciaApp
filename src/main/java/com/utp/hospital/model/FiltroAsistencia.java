package com.utp.hospital.model;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Criterios de búsqueda del listado. Todos son opcionales y se combinan con AND,
 * de modo que el resultado respeta TODOS los filtros activos a la vez.
 */
public class FiltroAsistencia implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer idPersonal;   // un colaborador específico
    private String texto;         // fragmento de DNI o de nombre
    private String cargo;         // Médico, Enfermera...
    private String turno;         // nombre del turno asignado al colaborador
    private String estado;        // Puntual / Tardanza / Permiso
    private String permiso;       // "Si" = con permiso, "No" = sin permiso
    private String tipoPermiso;   // nombre del tipo de permiso
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
