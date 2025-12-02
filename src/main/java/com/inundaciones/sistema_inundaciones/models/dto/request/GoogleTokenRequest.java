package com.inundaciones.sistema_inundaciones.models.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@NoArgsConstructor @AllArgsConstructor
@Getter @Setter @Builder
public class GoogleTokenRequest {
    @NotBlank(message = "El token de Google es requerido")
    private String token;
}
