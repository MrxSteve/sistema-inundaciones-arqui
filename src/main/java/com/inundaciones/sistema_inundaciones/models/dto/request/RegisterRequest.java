package com.inundaciones.sistema_inundaciones.models.dto.request;

import lombok.*;

@NoArgsConstructor @AllArgsConstructor
@Getter @Setter @Builder
public class RegisterRequest {
    private String nombre;
    private String email;
    private String telefono;
}