package com.aliaga.fittrack.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "historial_entrenamientos")
public class HistorialEntrenamiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime fechaHora; // Guarda fecha y hora exacta

    private String nombreRutina; // Ej: "Push Day - Semana 4" (Texto libre por si borras la rutina original)
    
    private String notasGenerales; // Ej: "Me sentí cansado hoy"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    @JsonIgnore
    private Usuario usuario;

    // Un Entreno tiene muchos Ejercicios registrados
    @OneToMany(mappedBy = "historial", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RegistroEjercicio> ejerciciosRealizados = new ArrayList<>();
}