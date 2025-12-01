package com.inundaciones.sistema_inundaciones.models.entities;

import com.inundaciones.sistema_inundaciones.models.enums.TipoAlerta;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "alertas")
@NoArgsConstructor @AllArgsConstructor
@Getter @Setter @Builder
public class Alerta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "distancia_cm", nullable = false, precision = 5, scale = 2)
    private BigDecimal distanciaCm;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_alerta", nullable = false)
    private TipoAlerta tipoAlerta;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String mensaje;

    @Builder.Default
    @Column(name = "usuarios_notificados")
    private Integer usuariosNotificados = 0;

    @Builder.Default
    @Column(name = "emails_enviados")
    private Integer emailsEnviados = 0;

    @Builder.Default
    @Column(name = "sms_enviados")
    private Integer smsEnviados = 0;

    @Builder.Default
    @Column(name = "costo_total", precision = 10, scale = 2)
    private BigDecimal costoTotal = BigDecimal.ZERO;

    @Builder.Default
    @Column(nullable = false)
    private Boolean procesada = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}