package com.inundaciones.sistema_inundaciones.utils.mappers;

import com.inundaciones.sistema_inundaciones.models.dto.request.UsuarioRequest;
import com.inundaciones.sistema_inundaciones.models.dto.response.UsuarioResponse;
import com.inundaciones.sistema_inundaciones.models.entities.Usuario;
import org.mapstruct.*;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = RolMapperHelper.class)
public interface UsuarioMapper {

    @Mapping(source = "roles", target = "roles")
    UsuarioResponse toResponse(Usuario usuario);

    Usuario toEntity(UsuarioRequest request);

    @Mapping(target = "roles", ignore = true)
    void updateEntity(UsuarioRequest request, @MappingTarget Usuario usuario);
}