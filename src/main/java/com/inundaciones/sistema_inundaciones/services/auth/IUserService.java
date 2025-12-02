package com.inundaciones.sistema_inundaciones.services.auth;

import com.inundaciones.sistema_inundaciones.models.dto.response.UsuarioResponse;
import com.inundaciones.sistema_inundaciones.models.entities.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface IUserService {
    Page<UsuarioResponse> findAll(Pageable pageable);
    Page<UsuarioResponse> buscarPorNombreContaining(String nombre, Pageable pageable);
    Optional<UsuarioResponse> buscarPorEmail(String email);
    Optional<UsuarioResponse> buscarPorId(Long id);

    Usuario buscarEntidadPorEmail(String email);
    Usuario buscarEntidadPorId(Long id);

    UsuarioResponse activar(Long id);
    UsuarioResponse desactivar(Long id);
    void eliminar(Long id);
}
