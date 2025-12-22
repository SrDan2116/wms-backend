package com.aliaga.fittrack.entity;

import com.aliaga.fittrack.enums.Genero;
import com.aliaga.fittrack.enums.Intensidad;
import com.aliaga.fittrack.enums.NivelActividad;
import com.aliaga.fittrack.enums.Objetivo;
import com.aliaga.fittrack.enums.Role;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "usuarios")
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
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

    // --- CONFIGURACIÓN ---
    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_actividad")
    private NivelActividad nivelActividad;

    @Enumerated(EnumType.STRING)
    private Objetivo objetivo;

    @Enumerated(EnumType.STRING)
    @Column(name = "intensidad_objetivo")
    private Intensidad intensidadObjetivo;
    
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "es_manual")
    private boolean esManual = false;

    // --- MACROS ---
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

    // --- SISTEMA DE SUSPENSIÓN (CORREGIDO) ---
    // Usamos 'Boolean' (objeto) en vez de 'boolean' (primitivo) para aceptar NULLs de la BD sin error
    @Column(name = "is_suspended")
    private Boolean isSuspended = false; 

    @Column(name = "suspension_reason")
    private String suspensionReason;

    @Column(name = "suspension_ends_at")
    private LocalDateTime suspensionEndsAt;
    
    // Método auxiliar seguro para evitar NullPointerException en el resto de la app
    public boolean isSuspended() {
        return Boolean.TRUE.equals(this.isSuspended);
    }
    
    public void setSuspended(boolean suspended) {
        this.isSuspended = suspended;
    }

    // =================================================================
    // MÉTODOS DE SECURITY
    // =================================================================

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() {
        // Usamos nuestro método auxiliar seguro
        return !isSuspended();
    }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}