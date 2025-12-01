package com.inundaciones.sistema_inundaciones.models.enums;

public enum TipoAlerta {
    RIESGO("Riesgo"),
    EMERGENCIA("Emergencia"),
    NORMALIZADO("Normalizado");

    private final String descripcion;

    TipoAlerta(String descripcion) {
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