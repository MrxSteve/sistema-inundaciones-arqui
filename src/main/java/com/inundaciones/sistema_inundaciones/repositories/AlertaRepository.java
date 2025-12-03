package com.inundaciones.sistema_inundaciones.repositories;

import com.inundaciones.sistema_inundaciones.models.entities.Alerta;
import com.inundaciones.sistema_inundaciones.models.enums.EstadoAlerta;
import com.inundaciones.sistema_inundaciones.models.enums.TipoAlerta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AlertaRepository extends JpaRepository<Alerta, Long> {
    Page<Alerta> findAllByOrderByFechaCreacionDesc(Pageable pageable);
    Page<Alerta> findByTipoOrderByFechaCreacionDesc(TipoAlerta tipo, Pageable pageable);
    List<Alerta> findByEstadoOrderByFechaCreacionDesc(EstadoAlerta estado);
}