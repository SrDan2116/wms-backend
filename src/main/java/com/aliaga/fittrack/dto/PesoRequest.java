package com.aliaga.fittrack.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PesoRequest {
    private BigDecimal valor;
    private LocalDate fecha;
}