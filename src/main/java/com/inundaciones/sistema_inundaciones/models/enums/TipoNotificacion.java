package com.inundaciones.sistema_inundaciones.models.enums;

public enum TipoNotificacion {
    SMS("SMS"),
    EMAIL("Email"),
    AMBOS("Ambos");

    private final String descripcion;

    TipoNotificacion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    @Override
    public String toString() {
        return descripcion;
    }
}