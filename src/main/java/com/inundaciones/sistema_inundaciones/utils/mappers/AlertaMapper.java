package com.inundaciones.sistema_inundaciones.utils.mappers;

import com.inundaciones.sistema_inundaciones.models.dto.request.AlertaRequest;
import com.inundaciones.sistema_inundaciones.models.dto.response.AlertaResponse;
import com.inundaciones.sistema_inundaciones.models.entities.Alerta;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AlertaMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaResolucion", ignore = true)
    @Mapping(target = "emailsEnviados", ignore = true)
    @Mapping(target = "smsEnviados", ignore = true)
    Alerta toEntity(AlertaRequest request);

    AlertaResponse toResponse(Alerta alerta);
}
