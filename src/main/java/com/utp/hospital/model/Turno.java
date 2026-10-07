package com.utp.hospital.model;

import java.io.Serializable;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/** DTO de la tabla turno. */
public class Turno implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter HM = DateTimeFormatter.ofPattern("HH:mm");

    private int idTurno;
    private String nombre;
    private LocalTime horaInicio;
    private LocalTime horaFin;

    public Turno() {
    }

    public Turno(int idTurno, String nombre, LocalTime horaInicio, LocalTime horaFin) {
        this.idTurno = idTurno;
        this.nombre = nombre;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
    }

    /** Texto para listas desplegables, por ejemplo: "Mañana (08:00 - 16:00)". */
    public String getEtiqueta() {
        return nombre + " (" + horaInicio.format(HM) + " - " + horaFin.format(HM) + ")";
    }

    /** Indica si la hora dada cae dentro del turno (soporta turnos que cruzan la medianoche). */
    public boolean contiene(LocalTime hora) {
        if (horaInicio.isBefore(horaFin)) {
            return !hora.isBefore(horaInicio) && hora.isBefore(horaFin);
        }
        return !hora.isBefore(horaInicio) || hora.isBefore(horaFin);
    }

    public int getIdTurno() { return idTurno; }
    public void setIdTurno(int idTurno) { this.idTurno = idTurno; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }
    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }
}
