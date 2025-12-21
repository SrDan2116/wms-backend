package com.aliaga.fittrack.dto;

import com.aliaga.fittrack.enums.Genero;
import com.aliaga.fittrack.enums.Intensidad;
import com.aliaga.fittrack.enums.NivelActividad;
import com.aliaga.fittrack.enums.Objetivo;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class RegisterRequest {
    private String nombre;
    private String email;
    private String password;
    
    // Datos Físicos
    private LocalDate fechaNacimiento;
    private Genero genero;
    private Integer alturaCm;
    private BigDecimal pesoInicial;
    
    // Configuración
    private NivelActividad nivelActividad;
    private Objetivo objetivo;
    private Intensidad intensidadObjetivo;
}