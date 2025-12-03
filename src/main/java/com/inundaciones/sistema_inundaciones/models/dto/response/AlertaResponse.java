package com.inundaciones.sistema_inundaciones.models.dto.response;

import com.inundaciones.sistema_inundaciones.models.enums.TipoAlerta;
import com.inundaciones.sistema_inundaciones.models.enums.EstadoAlerta;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor @AllArgsConstructor
@Getter @Setter @Builder
public class AlertaResponse {
    private Long id;
    private TipoAlerta tipo;
    private EstadoAlerta estado;
    private String mensaje;
    private Float distanciaDetectada;
    private String ubicacion;
    private Double latitud;
    private Double longitud;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaResolucion;
    private Integer emailsEnviados;
    private Integer smsEnviados;
    private String dispositivoId;
    private String observaciones;
}
