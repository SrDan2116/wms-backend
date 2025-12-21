package com.aliaga.fittrack.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "series_realizadas")
public class Serie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int numeroSerie; // 1, 2, 3...
    
    private BigDecimal peso; // Lo que cargaste (kg/lbs)
    
    private int repeticiones; // Lo que lograste hacer
    
    private int rpe; // (Opcional) Esfuerzo percibido del 1 al 10

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registro_ejercicio_id")
    @JsonIgnore
    private RegistroEjercicio registroEjercicio;
}