package com.aliaga.fittrack.controller;

import com.aliaga.fittrack.dto.SolicitudEliminacionRequest;
import com.aliaga.fittrack.dto.UpdateProfileRequest;
import com.aliaga.fittrack.entity.SolicitudEliminacion;
import com.aliaga.fittrack.entity.Usuario;
import com.aliaga.fittrack.enums.Intensidad;
import com.aliaga.fittrack.enums.NivelActividad;
import com.aliaga.fittrack.enums.Objetivo;
import com.aliaga.fittrack.repository.SolicitudEliminacionRepository;
import com.aliaga.fittrack.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.Period;

@RestController
@RequestMapping("/api/usuario")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;
    @Autowired
    private final SolicitudEliminacionRepository solicitudRepository;

    @GetMapping("/me")
    public ResponseEntity<Usuario> miPerfil() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return ResponseEntity.ok(usuario);
    }

    @PutMapping("/me")
    public ResponseEntity<Usuario> actualizarPerfil(@RequestBody UpdateProfileRequest req) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario u = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        u.setNombre(req.getNombre());
        u.setFechaNacimiento(req.getFechaNacimiento());
        u.setGenero(req.getGenero());
        u.setAlturaCm(req.getAlturaCm());
        u.setPesoInicial(req.getPesoActual());
        u.setNivelActividad(req.getNivelActividad());
        u.setObjetivo(req.getObjetivo());
        
        // Asignamos la Intensidad
        u.setIntensidadObjetivo(req.getIntensidadObjetivo());
        
        u.setEsManual(req.isEsManual());

        if (req.isEsManual()) {
            u.setCaloriasObjetivo(req.getCaloriasObjetivo());
            u.setProteinasObjetivo(req.getProteinasObjetivo());
            u.setCarbohidratosObjetivo(req.getCarbohidratosObjetivo());
            u.setGrasasObjetivo(req.getGrasasObjetivo());
        } else {
            recalcularMacros(u);
        }

        usuarioRepository.save(u);
        return ResponseEntity.ok(u);
    }

    private void recalcularMacros(Usuario u) {
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

        // Ahora Java reconoce estos valores porque importamos com.aliaga.fittrack.enums.Intensidad
        int ajuste = switch (u.getIntensidadObjetivo()) {
            case LENTO -> 100;
            case NORMAL -> 300;
            case AGRESIVO -> 500;
        };

        switch (u.getObjetivo()) {
            case PERDER_GRASA, RECOMPOSICION_CORPORAL -> caloriasFinales -= ajuste;
            case GANAR_MASA -> caloriasFinales += ajuste;
            case MANTENER -> { }
        }

        int proteinas = (int) (u.getPesoInicial().doubleValue() * 2.0);
        int grasas = (int) (u.getPesoInicial().doubleValue() * 0.9);
        int carbohidratos = Math.max(0, ((int) caloriasFinales - (proteinas * 4) - (grasas * 9)) / 4);

        u.setCaloriasObjetivo((int) caloriasFinales);
        u.setProteinasObjetivo(proteinas);
        u.setGrasasObjetivo(grasas);
        u.setCarbohidratosObjetivo(carbohidratos);
    }
    
    @PostMapping("/solicitar-eliminacion")
    public ResponseEntity<String> solicitarEliminacion(@RequestBody SolicitudEliminacionRequest req) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow();

        if (solicitudRepository.existsByUsuarioEmail(email)) {
            return ResponseEntity.badRequest().body("Ya tienes una solicitud en proceso.");
        }

        SolicitudEliminacion solicitud = SolicitudEliminacion.builder()
                .usuario(usuario)
                .motivo(req.getMotivo())
                .build();

        solicitudRepository.save(solicitud);
        return ResponseEntity.ok("Solicitud enviada al administrador.");
    }
    
    // Para mantener vivo el server
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("Pong!"); 
    }
}