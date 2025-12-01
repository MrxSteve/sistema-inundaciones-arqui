package com.inundaciones.sistema_inundaciones.models.dto.request;

import com.inundaciones.sistema_inundaciones.models.enums.TipoNotificacion;
import lombok.*;

@NoArgsConstructor @AllArgsConstructor
@Getter @Setter @Builder
public class UsuarioRequest {
    private String nombre;
    private String telefono;
    private TipoNotificacion tipoNotificacion;
}