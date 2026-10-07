package com.utp.hospital.model;

import java.io.Serializable;

/** Datos que el formulario entrega a la capa DAO para registrar una asistencia (con permiso opcional). */
public class SolicitudRegistro implements Serializable {

    private static final long serialVersionUID = 1L;

    private String dni;
    private String nombre;
    private String cargo;
    private Integer idTurno;          // solo se usa si el colaborador aún no existe
    private Integer idTipoPermiso;    // null = sin permiso
    private int dias = 1;
    private String motivo;
    private String nombreArchivo;     // nombre original del adjunto (opcional)
    private String rutaArchivo;       // ruta donde quedó guardado (opcional)

    public boolean tienePermiso() {
        return idTipoPermiso != null;
    }

    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getCargo() { return cargo; }
    public void setCargo(String cargo) { this.cargo = cargo; }
    public Integer getIdTurno() { return idTurno; }
    public void setIdTurno(Integer idTurno) { this.idTurno = idTurno; }
    public Integer getIdTipoPermiso() { return idTipoPermiso; }
    public void setIdTipoPermiso(Integer idTipoPermiso) { this.idTipoPermiso = idTipoPermiso; }
    public int getDias() { return dias; }
    public void setDias(int dias) { this.dias = dias; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public String getNombreArchivo() { return nombreArchivo; }
    public void setNombreArchivo(String nombreArchivo) { this.nombreArchivo = nombreArchivo; }
    public String getRutaArchivo() { return rutaArchivo; }
    public void setRutaArchivo(String rutaArchivo) { this.rutaArchivo = rutaArchivo; }
}
