package com.aliaga.fittrack.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class FinalizarEntrenoRequest {
    
    private String nombreRutina; // "Torso Pierna - Día 1"
    private String notasGenerales;
    private LocalDateTime fechaHora; // Permite guardar entrenos pasados o usar Now()
    
    private List<EjercicioRealizadoDTO> ejercicios;

    @Data
    public static class EjercicioRealizadoDTO {
        private String nombre;
        private List<SerieDTO> series;
    }

    @Data
    public static class SerieDTO {
        private BigDecimal peso;
        private int repeticiones;
        private int rpe;
    }
}