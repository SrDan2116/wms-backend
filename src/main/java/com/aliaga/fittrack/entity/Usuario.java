package com.aliaga.fittrack.entity;

import com.aliaga.fittrack.enums.Genero;
import com.aliaga.fittrack.enums.Intensidad;
import com.aliaga.fittrack.enums.NivelActividad;
import com.aliaga.fittrack.enums.Objetivo;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Data
@Entity
@Table(name = "usuarios")
// 1. AÑADIMOS ESTO: "implements UserDetails"
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) // <--- AGREGAR ESTO
    private String password;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    // --- DATOS FÍSICOS ---
    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Enumerated(EnumType.STRING)
    private Genero genero;

    @Column(name = "altura_cm")
    private Integer alturaCm;

    @Column(name = "peso_inicial")
    private BigDecimal pesoInicial;

    // --- CONFIGURACIÓN TDEE Y OBJETIVOS ---
    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_actividad")
    private NivelActividad nivelActividad;

    @Enumerated(EnumType.STRING)
    private Objetivo objetivo;

    @Enumerated(EnumType.STRING)
    @Column(name = "intensidad_objetivo")
    private Intensidad intensidadObjetivo; 

    @Column(name = "es_manual")
    private boolean esManual = false; 

    // --- MACROS CALCULADOS U OBJETIVO ---
    @Column(name = "calorias_objetivo")
    private Integer caloriasObjetivo;

    @Column(name = "proteinas_objetivo")
    private Integer proteinasObjetivo;

    @Column(name = "carbohidratos_objetivo")
    private Integer carbohidratosObjetivo;

    @Column(name = "grasas_objetivo")
    private Integer grasasObjetivo;

    @PrePersist
    protected void onCreate() {
        fechaRegistro = LocalDateTime.now();
    }

    // =================================================================
    // MÉTODOS DE SECURITY (OBLIGATORIOS PARA UserDetails)
    // =================================================================

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Por ahora todos son usuarios normales (ROLE_USER)
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getUsername() {
        // Para nosotros, el "username" es el email
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}