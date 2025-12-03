package com.inundaciones.sistema_inundaciones.services.alert;

import com.inundaciones.sistema_inundaciones.models.dto.request.AlertaRequest;
import com.inundaciones.sistema_inundaciones.models.dto.response.AlertaResponse;
import com.inundaciones.sistema_inundaciones.models.enums.TipoAlerta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IAlertaService {
    AlertaResponse crearAlerta(AlertaRequest request);
    AlertaResponse procesarAlertaEsp32(AlertaRequest request);
    Page<AlertaResponse> obtenerAlertas(Pageable pageable);
    Page<AlertaResponse> obtenerAlertasPorTipo(TipoAlerta tipo, Pageable pageable);
    AlertaResponse obtenerAlertaPorId(Long id);
    AlertaResponse resolverAlerta(Long id, String observaciones);
    List<AlertaResponse> obtenerAlertasActivas();
    List<AlertaResponse> obtenerAlertasResueltas();
}
