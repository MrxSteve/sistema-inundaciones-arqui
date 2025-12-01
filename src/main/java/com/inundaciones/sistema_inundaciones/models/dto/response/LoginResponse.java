package com.inundaciones.sistema_inundaciones.models.dto.response;

import lombok.*;

@NoArgsConstructor @AllArgsConstructor
@Getter @Setter @Builder
public class LoginResponse {
    private String token;
    private String tipo;
}