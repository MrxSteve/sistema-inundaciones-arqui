package com.inundaciones.sistema_inundaciones.repositories;

import com.inundaciones.sistema_inundaciones.models.entities.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    Optional<Usuario> findByGoogleId(String googleId);

    Page<Usuario> findByNombreContainingIgnoreCase(String nombre, Pageable pageable);

    boolean existsByEmail(String email);

    List<Usuario> findByActivoTrue();

    List<Usuario> findByActivoTrueAndTelefonoIsNotNull();
}
