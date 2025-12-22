package com.aliaga.fittrack.dto;

import lombok.Data;
import java.util.List;

@Data
public class RutinaRequest {
    private String nombre;
    private String descripcion;
    private List<DiaRequest> dias;

    @Data
    public static class DiaRequest {
        private String nombre;
        private List<EjercicioRequest> ejercicios;
    }

    @Data
    public static class EjercicioRequest {
        private String nombre;
        private int seriesObjetivo;
        private String repeticionesObjetivo; // String aquí también
        private String notas;
    }
}