package com.inundaciones.sistema_inundaciones.models.entities;

import com.inundaciones.sistema_inundaciones.models.enums.TipoNotificacion;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "usuarios")
@NoArgsConstructor @AllArgsConstructor
@Getter @Setter @Builder
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "password")
    private String password;

    @Column(length = 20)
    private String telefono;

    @Column(name = "google_id", unique = true, length = 100)
    private String googleId;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Builder.Default
    @Column(nullable = false)
    private Boolean activo = true;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "usuarios_roles",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "rol_id")
    )
    @Builder.Default
    @ToString.Exclude
    private List<Rol> roles = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_notificacion", nullable = false)
    @Builder.Default
    private TipoNotificacion tipoNotificacion = TipoNotificacion.AMBOS;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}