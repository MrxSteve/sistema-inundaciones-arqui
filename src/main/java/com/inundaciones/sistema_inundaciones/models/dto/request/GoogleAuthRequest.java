package com.inundaciones.sistema_inundaciones.models.dto.request;

import lombok.*;

@NoArgsConstructor @AllArgsConstructor
@Getter @Setter @Builder
public class GoogleAuthRequest {
    private String googleToken;
}