package com.inundaciones.sistema_inundaciones.models.dto.request;

import lombok.*;

import java.math.BigDecimal;

@NoArgsConstructor @AllArgsConstructor
@Getter @Setter @Builder
public class AlertaRequest {
    private BigDecimal distanciaCm;
    private String mensaje;
}