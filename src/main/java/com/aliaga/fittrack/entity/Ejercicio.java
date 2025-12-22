package com.aliaga.fittrack.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ejercicios_planificados") // Nombre en BD para no confundir con el historial
public class Ejercicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre; // Ej: "Sentadilla Hack"

    private int seriesObjetivo; // Ej: 4
    
    // CAMBIO CLAVE: String para permitir rangos como "10-12" o "Fallo"
    private String repeticionesObjetivo; 

    private String notas; // Ej: "Pies juntos"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dia_rutina_id")
    @JsonIgnore
    private DiaRutina diaRutina;
}