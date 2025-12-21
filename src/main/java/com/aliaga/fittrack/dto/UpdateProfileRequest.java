package com.aliaga.fittrack.dto;

// --- CORRECCIÓN: Importamos desde el paquete 'enums' ---
import com.aliaga.fittrack.enums.Genero;
import com.aliaga.fittrack.enums.Intensidad;
import com.aliaga.fittrack.enums.NivelActividad;
import com.aliaga.fittrack.enums.Objetivo;
// ------------------------------------------------------

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class UpdateProfileRequest {
    
    private String nombre;
    private LocalDate fechaNacimiento;
    private Genero genero;
    private Integer alturaCm;
    private BigDecimal pesoActual;
    
    private NivelActividad nivelActividad;
    private Objetivo objetivo;
    private Intensidad intensidadObjetivo; 

    private boolean esManual;
    
    private Integer caloriasObjetivo;
    private Integer proteinasObjetivo;
    private Integer carbohidratosObjetivo;
    private Integer grasasObjetivo;
}