package com.inundaciones.sistema_inundaciones.repositories;

import com.inundaciones.sistema_inundaciones.models.entities.Alerta;
import com.inundaciones.sistema_inundaciones.models.enums.TipoAlerta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;

public interface AlertaRepository extends JpaRepository<Alerta, Long> {
    Page<Alerta> findByTipoAlerta(TipoAlerta tipoAlerta, Pageable pageable);
    
    @Query("SELECT COUNT(a) FROM Alerta a WHERE a.createdAt >= :desde")
    Long countAlertasDesde(LocalDateTime desde);
    
    @Query("SELECT COUNT(a) FROM Alerta a WHERE a.tipoAlerta = :tipo AND a.createdAt >= :desde")
    Long countByTipoDesde(TipoAlerta tipo, LocalDateTime desde);
}