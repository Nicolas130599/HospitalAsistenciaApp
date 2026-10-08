package com.utp.hospital.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;


public class Asistencia implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    private int idAsistencia;
    private int idPersonal;
    private String dni;
    private String nombre;
    private String cargo;
    private String turno;
    private LocalDate fecha;
    private LocalTime horaEntrada;
    private LocalTime horaSalida;
    private String estado;
    private boolean conPermiso;
    private String tipoPermiso;
    private int dias;
    private LocalDate permisoDesde;
    private LocalDate permisoHasta;
    private String observacion;
    private String documento;

    public String getPermisoTexto() {
        return conPermiso ? "Sí" : "No";
    }

    public String getFechaFormateada() {
        return fecha == null ? "" : fecha.format(FECHA);
    }

    public String getHoraEntradaFormateada() {
        return horaEntrada == null ? "" : horaEntrada.format(HORA);
    }

    public String getHoraSalidaFormateada() {
        return horaSalida == null ? "" : horaSalida.format(HORA);
    }

    public String getPeriodoPermiso() {
        if (!conPermiso || permisoDesde == null || permisoHasta == null) {
            return "";
        }
        return permisoDesde.format(FECHA) + " al " + permisoHasta.format(FECHA);
    }

    public String getClaseEstado() {
        if ("Puntual".equals(estado)) {
            return "success";
        }
        if ("Tardanza".equals(estado)) {
            return "warning";
        }
        return "info";
    }

    public int getIdAsistencia() { return idAsistencia; }
    public void setIdAsistencia(int idAsistencia) { this.idAsistencia = idAsistencia; }
    public int getIdPersonal() { return idPersonal; }
    public void setIdPersonal(int idPersonal) { this.idPersonal = idPersonal; }
    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getCargo() { return cargo; }
    public void setCargo(String cargo) { this.cargo = cargo; }
    public String getTurno() { return turno; }
    public void setTurno(String turno) { this.turno = turno; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public LocalTime getHoraEntrada() { return horaEntrada; }
    public void setHoraEntrada(LocalTime horaEntrada) { this.horaEntrada = horaEntrada; }
    public LocalTime getHoraSalida() { return horaSalida; }
    public void setHoraSalida(LocalTime horaSalida) { this.horaSalida = horaSalida; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public boolean isConPermiso() { return conPermiso; }
    public void setConPermiso(boolean conPermiso) { this.conPermiso = conPermiso; }
    public String getTipoPermiso() { return tipoPermiso; }
    public void setTipoPermiso(String tipoPermiso) { this.tipoPermiso = tipoPermiso; }
    public int getDias() { return dias; }
    public void setDias(int dias) { this.dias = dias; }
    public LocalDate getPermisoDesde() { return permisoDesde; }
    public void setPermisoDesde(LocalDate permisoDesde) { this.permisoDesde = permisoDesde; }
    public LocalDate getPermisoHasta() { return permisoHasta; }
    public void setPermisoHasta(LocalDate permisoHasta) { this.permisoHasta = permisoHasta; }
    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }
    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }
}
