package com.inundaciones.sistema_inundaciones.models.dto.response;

import com.inundaciones.sistema_inundaciones.models.enums.TipoNotificacion;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor @AllArgsConstructor
@Getter @Setter @Builder
public class UsuarioResponse {
    private Long id;
    private String nombre;
    private String email;
    private String telefono;
    private String avatarUrl;
    private Boolean activo;
    private TipoNotificacion tipoNotificacion;
    private LocalDateTime createdAt;
}