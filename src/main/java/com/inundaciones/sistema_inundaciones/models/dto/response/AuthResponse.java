package com.inundaciones.sistema_inundaciones.models.dto.response;

import lombok.*;

@NoArgsConstructor @AllArgsConstructor
@Getter @Setter @Builder
public class AuthResponse {
    private String token;
    @Builder.Default
    private String tokenType = "Bearer";
    private UsuarioResponse usuario;
}
