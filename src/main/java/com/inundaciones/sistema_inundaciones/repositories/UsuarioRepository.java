package com.inundaciones.sistema_inundaciones.repositories;

import com.inundaciones.sistema_inundaciones.models.entities.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Page<Usuario> findAll(Pageable pageable);
    Optional<Usuario> findByEmail(String email);
    Optional<Usuario> findByGoogleId(String googleId);

    boolean existsByEmail(String email);

    List<Usuario> findByActivoTrue();
    Page<Usuario> findByNombreContainingIgnoreCase(String nombre, Pageable pageable);
}