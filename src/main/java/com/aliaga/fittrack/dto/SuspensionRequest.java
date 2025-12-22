package com.aliaga.fittrack.dto;

import lombok.Data;

@Data
public class SuspensionRequest {
    private Long userId;
    private String reason;
    
    // Duración
    private int durationValue; // Ej: 2
    private String durationUnit; // "HOURS", "DAYS", "WEEKS", "MONTHS", "PERMANENT"
}