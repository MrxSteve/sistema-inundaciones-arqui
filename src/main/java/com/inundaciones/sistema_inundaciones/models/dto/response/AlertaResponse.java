package com.inundaciones.sistema_inundaciones.models.dto.response;

import com.inundaciones.sistema_inundaciones.models.enums.TipoAlerta;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@NoArgsConstructor @AllArgsConstructor
@Getter @Setter @Builder
public class AlertaResponse {
    private Long id;
    private BigDecimal distanciaCm;
    private TipoAlerta tipoAlerta;
    private String mensaje;
    private Integer usuariosNotificados;
    private Integer emailsEnviados;
    private Integer smsEnviados;
    private BigDecimal costoTotal;
    private Boolean procesada;
    private LocalDateTime createdAt;
}