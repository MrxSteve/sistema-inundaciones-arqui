package com.inundaciones.sistema_inundaciones.models.enums;

public enum EstadoAlerta {
    ACTIVA("Activa", "La alerta está vigente"),
    RESUELTA("Resuelta", "La situación ha sido normalizada"),
    CANCELADA("Cancelada", "La alerta fue cancelada manualmente");

    private final String nombre;
    private final String descripcion;

    EstadoAlerta(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
