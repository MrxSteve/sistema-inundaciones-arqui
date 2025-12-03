package com.inundaciones.sistema_inundaciones.models.entities;

import com.inundaciones.sistema_inundaciones.models.enums.TipoAlerta;
import com.inundaciones.sistema_inundaciones.models.enums.EstadoAlerta;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "alertas")
@NoArgsConstructor @AllArgsConstructor
@Getter @Setter @Builder
public class Alerta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoAlerta tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EstadoAlerta estado = EstadoAlerta.ACTIVA;

    @Column(nullable = false, length = 500)
    private String mensaje;

    @Column(name = "distancia_cm", nullable = false)
    private Float distanciaDetectada;

    @Column(name = "ubicacion", length = 200)
    private String ubicacion;

    @Column(name = "latitud")
    private Double latitud;

    @Column(name = "longitud")
    private Double longitud;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_resolucion")
    private LocalDateTime fechaResolucion;

    @Builder.Default
    @Column(name = "emails_enviados", nullable = false)
    private Integer emailsEnviados = 0;

    @Builder.Default
    @Column(name = "sms_enviados", nullable = false)
    private Integer smsEnviados = 0;

    @Column(name = "dispositivo_id", length = 50)
    private String dispositivoId; // ID del ESP32 que envía la alerta

    @Column(name = "observaciones", length = 1000)
    private String observaciones;

    // Método para marcar como resuelta
    public void resolver(String observaciones) {
        this.estado = EstadoAlerta.RESUELTA;
        this.fechaResolucion = LocalDateTime.now();
        this.observaciones = observaciones;
    }
}
