package com.inundaciones.sistema_inundaciones.services.auth;

import com.inundaciones.sistema_inundaciones.models.entities.Rol;
import com.inundaciones.sistema_inundaciones.repositories.RolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RolServiceImpl implements IRolService {
    private final RolRepository rolRepository;

    @Override
    public boolean existePorNombre(String nombre) {
        return rolRepository.existsByNombreIgnoreCase(nombre);
    }

    @Override
    public Rol buscarEntidadPorId(Long id) {
        return rolRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado con id: " + id));
    }

    @Override
    public Rol buscarEntidadPorNombre(String nombre) {
        return rolRepository.findByNombreIgnoreCase(nombre)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado con nombre: " + nombre));
    }
}
