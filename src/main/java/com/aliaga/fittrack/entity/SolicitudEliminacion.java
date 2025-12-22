package com.aliaga.fittrack.entity;

import com.aliaga.fittrack.enums.EstadoSolicitud;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "solicitudes_eliminacion")
public class SolicitudEliminacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @Column(columnDefinition = "TEXT")
    private String motivo;

    @Enumerated(EnumType.STRING)
    private EstadoSolicitud estado;

    private LocalDateTime fechaSolicitud;

    @PrePersist
    protected void onCreate() {
        fechaSolicitud = LocalDateTime.now();
        if (estado == null) estado = EstadoSolicitud.PENDIENTE;
    }
}