package com.inundaciones.sistema_inundaciones.models.dto.request;

import com.inundaciones.sistema_inundaciones.models.enums.TipoAlerta;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.*;

@NoArgsConstructor @AllArgsConstructor
@Getter @Setter @Builder
public class AlertaRequest {
    @NotNull(message = "El tipo de alerta es requerido")
    private TipoAlerta tipo;

    @NotBlank(message = "El mensaje es requerido")
    private String mensaje;

    @NotNull(message = "La distancia detectada es requerida")
    @DecimalMin(value = "0.0", message = "La distancia debe ser mayor a 0")
    @DecimalMax(value = "500.0", message = "La distancia debe ser menor a 500 cm")
    private Float distanciaDetectada;

    private String ubicacion;

    private Double latitud;

    private Double longitud;

    private String dispositivoId;

    private String observaciones;
}
