package com.aliaga.fittrack.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "registros_ejercicios")
public class RegistroEjercicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombreEjercicio; // Ej: "Sentadilla Libre"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "historial_id")
    @JsonIgnore
    private HistorialEntrenamiento historial;

    // Un Ejercicio tiene muchas Series (Sets)
    @OneToMany(mappedBy = "registroEjercicio", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Serie> series = new ArrayList<>();
}