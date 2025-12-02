package com.inundaciones.sistema_inundaciones.utils.mappers;

import com.inundaciones.sistema_inundaciones.models.dto.request.AlertaRequest;
import com.inundaciones.sistema_inundaciones.models.dto.response.AlertaResponse;
import com.inundaciones.sistema_inundaciones.models.entities.Alerta;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AlertaMapper {
    AlertaResponse toResponse(Alerta alerta);
    Alerta toEntity(AlertaRequest request);
}