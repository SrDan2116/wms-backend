package com.aliaga.fittrack.dto;

import com.aliaga.fittrack.enums.Genero;
import com.aliaga.fittrack.enums.Intensidad;
import com.aliaga.fittrack.enums.NivelActividad;
import com.aliaga.fittrack.enums.Objetivo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

    // --- DATOS OBLIGATORIOS (Registro Rápido) ---
    private String nombre;
    private String email;
    private String password;
    
    // --- DATOS OPCIONALES (Se llenan después en el Perfil) ---
    // Nota: No usamos @NotNull para permitir que sean nulos al inicio
    private LocalDate fechaNacimiento;
    private Genero genero;
    private Integer alturaCm;
    private BigDecimal pesoInicial;
    
    // Configuración
    private NivelActividad nivelActividad;
    private Objetivo objetivo;
    private Intensidad intensidadObjetivo;
}