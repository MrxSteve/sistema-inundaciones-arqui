package com.inundaciones.sistema_inundaciones.services.auth;

import com.inundaciones.sistema_inundaciones.models.entities.Rol;

public interface IRolService {
    boolean existePorNombre(String nombre);
    Rol buscarEntidadPorId(Long id);
    Rol buscarEntidadPorNombre(String nombre);
}
