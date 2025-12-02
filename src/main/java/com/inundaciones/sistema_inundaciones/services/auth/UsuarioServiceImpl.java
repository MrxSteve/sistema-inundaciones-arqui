package com.inundaciones.sistema_inundaciones.services.auth;

import com.inundaciones.sistema_inundaciones.models.dto.response.UsuarioResponse;
import com.inundaciones.sistema_inundaciones.models.entities.Usuario;
import com.inundaciones.sistema_inundaciones.repositories.UsuarioRepository;
import com.inundaciones.sistema_inundaciones.utils.mappers.UsuarioMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements IUserService {
    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;

    @Override
    public Page<UsuarioResponse> findAll(Pageable pageable) {
        return usuarioRepository.findAll(pageable)
                .map(usuarioMapper::toResponse);
    }

    @Override
    public Page<UsuarioResponse> buscarPorNombreContaining(String nombre, Pageable pageable) {
        return usuarioRepository.findByNombreContainingIgnoreCase(nombre, pageable)
                .map(usuarioMapper::toResponse);
    }

    @Override
    public Optional<UsuarioResponse> buscarPorEmail(String email) {
        Usuario usuario = buscarEntidadPorEmail(email);
        return Optional.of(usuarioMapper.toResponse(usuario));
    }

    @Override
    public Optional<UsuarioResponse> buscarPorId(Long id) {
        Usuario usuario = buscarEntidadPorId(id);
        return Optional.of(usuarioMapper.toResponse(usuario));
    }

    @Override
    public Usuario buscarEntidadPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con email: " + email));
    }

    @Override
    public Usuario buscarEntidadPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con id: " + id));
    }

    @Override
    public UsuarioResponse activar(Long id) {
        Usuario usuario = buscarEntidadPorId(id);
        usuario.setActivo(true);
        return usuarioMapper.toResponse(usuarioRepository.save(usuario));
    }

    @Override
    public UsuarioResponse desactivar(Long id) {
        Usuario usuario = buscarEntidadPorId(id);
        usuario.setActivo(false);
        return usuarioMapper.toResponse(usuarioRepository.save(usuario));
    }

    @Override
    public void eliminar(Long id) {
        this.buscarEntidadPorId(id);
        usuarioRepository.deleteById(id);
    }
}
