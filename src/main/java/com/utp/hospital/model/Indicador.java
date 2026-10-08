package com.utp.hospital.model;

import java.io.Serializable;

public class Indicador implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String etiqueta;
    private final int valor;

    public Indicador(String etiqueta, int valor) {
        this.etiqueta = etiqueta;
        this.valor = valor;
    }

    public String getEtiqueta() { return etiqueta; }
    public int getValor() { return valor; }
}
