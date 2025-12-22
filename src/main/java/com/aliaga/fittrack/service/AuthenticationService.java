package com.aliaga.fittrack.service;

import com.aliaga.fittrack.dto.AuthenticationRequest;
import com.aliaga.fittrack.dto.AuthenticationResponse;
import com.aliaga.fittrack.dto.RegisterRequest;
import com.aliaga.fittrack.entity.PasswordResetToken;
import com.aliaga.fittrack.entity.Usuario;
import com.aliaga.fittrack.enums.Role;
import com.aliaga.fittrack.repository.PasswordTokenRepository;
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
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UsuarioRepository repository;
    private final PasswordTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    public AuthenticationResponse register(RegisterRequest request) {
        var usuario = new Usuario();
        usuario.setNombre(request.getNombre());
        usuario.setEmail(request.getEmail());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setRole(Role.CLIENTE);
        
        usuario.setFechaNacimiento(request.getFechaNacimiento());
        usuario.setGenero(request.getGenero());
        usuario.setAlturaCm(request.getAlturaCm());
        usuario.setPesoInicial(request.getPesoInicial());
        usuario.setNivelActividad(request.getNivelActividad());
        usuario.setObjetivo(request.getObjetivo());
        usuario.setIntensidadObjetivo(request.getIntensidadObjetivo());
        
        if (tieneDatosFisicosCompletos(usuario)) {
            calcularMacrosIniciales(usuario);
        } else {
            usuario.setCaloriasObjetivo(0);
            usuario.setProteinasObjetivo(0);
            usuario.setCarbohidratosObjetivo(0);
            usuario.setGrasasObjetivo(0);
        }

        repository.save(usuario);
        var jwtToken = jwtService.generateToken(usuario);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .nombreUsuario(usuario.getNombre())
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        var usuario = repository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (usuario.isSuspended()) {
            if (usuario.getSuspensionEndsAt() != null && LocalDateTime.now().isAfter(usuario.getSuspensionEndsAt())) {
                usuario.setSuspended(false);
                usuario.setSuspensionReason(null);
                usuario.setSuspensionEndsAt(null);
                repository.save(usuario);
            } else {
                String fechaFin = usuario.getSuspensionEndsAt() != null 
                    ? usuario.getSuspensionEndsAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) 
                    : "Indefinida";
                throw new RuntimeException("CUENTA SUSPENDIDA ⛔. Razón: " + usuario.getSuspensionReason() + 
                                           ". Acceso bloqueado hasta: " + fechaFin);
            }
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        usuario.setLastLogin(LocalDateTime.now());
        repository.save(usuario);

        var jwtToken = jwtService.generateToken(usuario);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .nombreUsuario(usuario.getNombre())
                .build();
    }

    // --- NUEVO: RECUPERACIÓN DE CONTRASEÑA ---

    public void forgotPassword(String email) {
        Usuario usuario = repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Si ya hay un token viejo, lo borramos
        tokenRepository.findByUsuario(usuario).ifPresent(tokenRepository::delete);

        // Crear nuevo token
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .usuario(usuario)
                .expiryDate(LocalDateTime.now().plusMinutes(15)) // 15 min de validez
                .build();
        
        tokenRepository.save(resetToken);

        // Enviar Email (Simulado)
        emailService.sendResetToken(email, token);
    }

    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token inválido"));

        if (resetToken.isExpired()) {
            throw new RuntimeException("Token expirado");
        }

        Usuario usuario = resetToken.getUsuario();
        usuario.setPassword(passwordEncoder.encode(newPassword));
        repository.save(usuario);

        // Borrar el token usado
        tokenRepository.delete(resetToken);
    }
    
    // --- MÉTODOS AUXILIARES (IGUAL QUE ANTES) ---
    private boolean tieneDatosFisicosCompletos(Usuario u) {
        return u.getPesoInicial() != null 
            && u.getAlturaCm() != null 
            && u.getFechaNacimiento() != null
            && u.getGenero() != null
            && u.getNivelActividad() != null;
    }

    private void calcularMacrosIniciales(Usuario u) {
        int edad = Period.between(u.getFechaNacimiento(), LocalDate.now()).getYears();
        double tmb = (10 * u.getPesoInicial().doubleValue()) + (6.25 * u.getAlturaCm()) - (5 * edad);
        if (u.getGenero().name().equals("MASCULINO")) tmb += 5; else tmb -= 161;

        double factor = switch (u.getNivelActividad()) {
            case SEDENTARIO -> 1.2;
            case LIGERO -> 1.375;
            case MODERADO -> 1.55;
            case ACTIVO -> 1.725;
            case MUY_ACTIVO -> 1.9;
        };
        double tdee = tmb * factor;
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
            case MANTENER -> { }
            case RECOMPOSICION_CORPORAL -> caloriasFinales -= ajusteCalorias;
        }

        int proteinas = (int) (u.getPesoInicial().doubleValue() * 2.0);
        int grasas = (int) (u.getPesoInicial().doubleValue() * 0.9);
        int carbohidratos = Math.max(0, ((int) caloriasFinales - (proteinas * 4) - (grasas * 9)) / 4);

        u.setCaloriasObjetivo((int) caloriasFinales);
        u.setProteinasObjetivo(proteinas);
        u.setGrasasObjetivo(grasas);
        u.setCarbohidratosObjetivo(carbohidratos);
        u.setEsManual(false);
    }
}