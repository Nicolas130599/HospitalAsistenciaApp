package com.utp.hospital.dao;

/** Error de regla de negocio (por ejemplo, asistencia duplicada); su mensaje se muestra al usuario. */
public class NegocioException extends Exception {

    private static final long serialVersionUID = 1L;

    public NegocioException(String mensaje) {
        super(mensaje);
    }
}
