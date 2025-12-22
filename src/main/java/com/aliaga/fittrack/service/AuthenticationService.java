package com.aliaga.fittrack.service;

import com.aliaga.fittrack.dto.AuthenticationRequest;
import com.aliaga.fittrack.dto.AuthenticationResponse;
import com.aliaga.fittrack.dto.RegisterRequest;
import com.aliaga.fittrack.entity.Usuario;
import com.aliaga.fittrack.enums.Role;
import com.aliaga.fittrack.repository.UsuarioRepository;
import com.aliaga.fittrack.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationResponse register(RegisterRequest request) {
        // 1. Crear el usuario base
        var usuario = new Usuario();
        usuario.setNombre(request.getNombre());
        usuario.setEmail(request.getEmail());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        
        // --- ASIGNACIÓN DE ROL POR DEFECTO ---
        usuario.setRole(Role.CLIENTE);
        
        // Asignar datos físicos (pueden ser null en registro rápido)
        usuario.setFechaNacimiento(request.getFechaNacimiento());
        usuario.setGenero(request.getGenero());
        usuario.setAlturaCm(request.getAlturaCm());
        usuario.setPesoInicial(request.getPesoInicial());
        usuario.setNivelActividad(request.getNivelActividad());
        usuario.setObjetivo(request.getObjetivo());
        usuario.setIntensidadObjetivo(request.getIntensidadObjetivo());
        
        // 2. LÓGICA CONDICIONAL: ¿Tenemos datos para calcular macros?
        if (tieneDatosFisicosCompletos(usuario)) {
            calcularMacrosIniciales(usuario);
        } else {
            // Si no hay datos, inicializamos en 0
            usuario.setCaloriasObjetivo(0);
            usuario.setProteinasObjetivo(0);
            usuario.setCarbohidratosObjetivo(0);
            usuario.setGrasasObjetivo(0);
        }

        // 3. Guardar y Generar Token
        repository.save(usuario);
        var jwtToken = jwtService.generateToken(usuario);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .nombreUsuario(usuario.getNombre())
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        // 1. Buscar usuario PRIMERO para verificar estado de suspensión
        // (Aun no autenticamos contraseña, primero vemos si existe y si puede entrar)
        var usuario = repository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // --- LÓGICA DE SUSPENSIÓN ---
        if (usuario.isSuspended()) {
            // Verificar si el tiempo de suspensión ya expiró
            if (usuario.getSuspensionEndsAt() != null && LocalDateTime.now().isAfter(usuario.getSuspensionEndsAt())) {
                // El castigo terminó: Levantamos la suspensión automáticamente
                usuario.setSuspended(false);
                usuario.setSuspensionReason(null);
                usuario.setSuspensionEndsAt(null);
                repository.save(usuario); // Guardamos el usuario "limpio"
            } else {
                // Sigue suspendido: Preparamos el mensaje de error
                String fechaFin = usuario.getSuspensionEndsAt() != null 
                    ? usuario.getSuspensionEndsAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) 
                    : "Indefinida";
                
                throw new RuntimeException("CUENTA SUSPENDIDA ⛔. Razón: " + usuario.getSuspensionReason() + 
                                           ". Acceso bloqueado hasta: " + fechaFin);
            }
        }

        // 2. Si pasa la suspensión, validamos contraseña con Spring Security
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        // 3. ACTUALIZAR ULTIMA CONEXIÓN
        usuario.setLastLogin(LocalDateTime.now());
        repository.save(usuario);

        // 4. Generar Token
        var jwtToken = jwtService.generateToken(usuario);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .nombreUsuario(usuario.getNombre())
                .build();
    }
    
    // Método auxiliar para verificar si se puede calcular
    private boolean tieneDatosFisicosCompletos(Usuario u) {
        return u.getPesoInicial() != null 
            && u.getAlturaCm() != null 
            && u.getFechaNacimiento() != null
            && u.getGenero() != null
            && u.getNivelActividad() != null;
    }

    // --- LÓGICA DE CÁLCULO DE MACROS ---
    private void calcularMacrosIniciales(Usuario u) {
        // Calcular Edad
        int edad = Period.between(u.getFechaNacimiento(), LocalDate.now()).getYears();
        
        // 1. TMB
        double tmb = (10 * u.getPesoInicial().doubleValue()) + (6.25 * u.getAlturaCm()) - (5 * edad);
        if (u.getGenero().name().equals("MASCULINO")) tmb += 5;
        else tmb -= 161;

        // 2. Factor de Actividad
        double factor = switch (u.getNivelActividad()) {
            case SEDENTARIO -> 1.2;
            case LIGERO -> 1.375;
            case MODERADO -> 1.55;
            case ACTIVO -> 1.725;
            case MUY_ACTIVO -> 1.9;
        };
        
        // 3. TDEE
        double tdee = tmb * factor;

        // 4. Ajuste según Objetivo
        double caloriasFinales = tdee;
        
        int ajusteCalorias = 300; 
        if (u.getIntensidadObjetivo() != null) {
            ajusteCalorias = switch (u.getIntensidadObjetivo()) {
                case LENTO -> 100;
                case NORMAL -> 300;
                case AGRESIVO -> 500;
            };
        }

        switch (u.getObjetivo()) {
            case PERDER_GRASA -> caloriasFinales -= ajusteCalorias;
            case GANAR_MASA -> caloriasFinales += ajusteCalorias;
            case MANTENER -> { /* TDEE */ }
            case RECOMPOSICION_CORPORAL -> caloriasFinales -= ajusteCalorias;
        }

        // 5. Reparto de Macros
        int proteinas = (int) (u.getPesoInicial().doubleValue() * 2.0);
        int grasas = (int) (u.getPesoInicial().doubleValue() * 0.9);
        
        int calProteina = proteinas * 4;
        int calGrasa = grasas * 9;
        
        int calRestantes = (int) caloriasFinales - calProteina - calGrasa;
        int carbohidratos = Math.max(0, calRestantes / 4);

        u.setCaloriasObjetivo((int) caloriasFinales);
        u.setProteinasObjetivo(proteinas);
        u.setGrasasObjetivo(grasas);
        u.setCarbohidratosObjetivo(carbohidratos);
        u.setEsManual(false);
    }
}