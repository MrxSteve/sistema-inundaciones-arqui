package com.inundaciones.sistema_inundaciones.models.dto.response;

import lombok.*;

import java.math.BigDecimal;

@NoArgsConstructor @AllArgsConstructor
@Getter @Setter @Builder
public class EstadisticasResponse {
    private Long totalAlertas;
    private Long emergencias;
    private Long riesgos;
    private Long normalizadas;
    private Integer totalUsuarios;
    private BigDecimal costoTotal;
}