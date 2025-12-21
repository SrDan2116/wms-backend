package com.aliaga.fittrack.service;

import com.aliaga.fittrack.dto.AuthenticationRequest;
import com.aliaga.fittrack.dto.AuthenticationResponse;
import com.aliaga.fittrack.dto.RegisterRequest;
import com.aliaga.fittrack.entity.Usuario;
import com.aliaga.fittrack.repository.UsuarioRepository;
import com.aliaga.fittrack.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;

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
        usuario.setFechaNacimiento(request.getFechaNacimiento());
        usuario.setGenero(request.getGenero());
        usuario.setAlturaCm(request.getAlturaCm());
        usuario.setPesoInicial(request.getPesoInicial());
        usuario.setNivelActividad(request.getNivelActividad());
        usuario.setObjetivo(request.getObjetivo());
        usuario.setIntensidadObjetivo(request.getIntensidadObjetivo());
        
        // 2. MAGIA: Calcular Macros Automáticamente (Fórmula Mifflin-St Jeor Ajustada)
        calcularMacrosIniciales(usuario);

        // 3. Guardar y Generar Token
        repository.save(usuario);
        var jwtToken = jwtService.generateToken(usuario);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .nombreUsuario(usuario.getNombre())
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        var usuario = repository.findByEmail(request.getEmail()).orElseThrow();
        var jwtToken = jwtService.generateToken(usuario);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .nombreUsuario(usuario.getNombre())
                .build();
    }

    // --- LÓGICA PRIVADA DE CÁLCULO DE MACROS ---
    private void calcularMacrosIniciales(Usuario u) {
        // Calcular Edad
        int edad = Period.between(u.getFechaNacimiento(), LocalDate.now()).getYears();
        
        // 1. TMB (Tasa Metabólica Basal) - Mifflin-St Jeor
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
        
        // 3. TDEE (Gasto Energético Total Diario)
        double tdee = tmb * factor;

        // 4. Ajuste según Objetivo (Superávit o Déficit)
        double caloriasFinales = tdee;
        
        // NUEVO AJUSTE: Valores más precisos basados en estudios (100, 300, 500)
        int ajusteCalorias = switch (u.getIntensidadObjetivo()) {
            case LENTO -> 100;    // Déficit/Superávit muy ligero
            case NORMAL -> 300;   // Estándar (Recomendado)
            case AGRESIVO -> 500; // Fuerte (Máximo recomendado para no perder músculo)
        };

        switch (u.getObjetivo()) {
            case PERDER_GRASA -> {
                caloriasFinales -= ajusteCalorias;
            }
            case GANAR_MASA -> {
                caloriasFinales += ajusteCalorias;
            }
            case MANTENER -> { 
                /* Se mantiene en TDEE exacto */ 
            }
            case RECOMPOSICION_CORPORAL -> {
                // LÓGICA ACTUALIZADA: 
                // La recomposición requiere un déficit leve para oxidar grasa 
                // mientras la proteína alta protege el músculo.
                caloriasFinales -= ajusteCalorias;
            }
        }

        // 5. Reparto de Macros (Aproximación Estándar Fitness)
        // Proteína: 2g por kg de peso (Para recomposición es vital mantenerla alta)
        int proteinas = (int) (u.getPesoInicial().doubleValue() * 2.0);
        
        // Grasas: 0.9g por kg (Salud hormonal)
        int grasas = (int) (u.getPesoInicial().doubleValue() * 0.9);
        
        // Carbohidratos: El resto de calorías (Energía para entrenar)
        // 1g Proteina = 4 cal, 1g Grasa = 9 cal, 1g Carbo = 4 cal
        int calProteina = proteinas * 4;
        int calGrasa = grasas * 9;
        
        // Evitar negativos si el déficit es muy agresivo en personas muy livianas
        int calRestantes = (int) caloriasFinales - calProteina - calGrasa;
        int carbohidratos = Math.max(0, calRestantes / 4);

        // Setear valores
        u.setCaloriasObjetivo((int) caloriasFinales);
        u.setProteinasObjetivo(proteinas);
        u.setGrasasObjetivo(grasas);
        u.setCarbohidratosObjetivo(carbohidratos);
        u.setEsManual(false);
    }
}