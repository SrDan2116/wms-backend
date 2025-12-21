package com.aliaga.fittrack.controller;

import com.aliaga.fittrack.dto.PesoRequest;
import com.aliaga.fittrack.entity.Peso;
import com.aliaga.fittrack.entity.Usuario;
import com.aliaga.fittrack.repository.PesoRepository;
import com.aliaga.fittrack.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/pesos")
@RequiredArgsConstructor
public class PesoController {

    private final PesoRepository pesoRepository;
    private final UsuarioRepository usuarioRepository;

    // --- OBTENER HISTORIAL (Para el Gráfico) ---
    @GetMapping
    public ResponseEntity<List<Peso>> obtenerHistorial() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Peso> historial = pesoRepository.findByUsuarioEmailOrderByFechaAsc(email);
        return ResponseEntity.ok(historial);
    }

    // --- REGISTRAR NUEVO PESO ---
    @PostMapping
    public ResponseEntity<Peso> registrarPeso(@RequestBody PesoRequest req) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Si no envía fecha, usamos la de hoy
        LocalDate fechaRegistro = (req.getFecha() != null) ? req.getFecha() : LocalDate.now();

        Peso nuevoPeso = Peso.builder()
                .valor(req.getValor())
                .fecha(fechaRegistro)
                .usuario(usuario)
                .build();

        pesoRepository.save(nuevoPeso);
        
        // Opcional: Actualizar el "peso actual" del perfil del usuario también
        usuario.setPesoInicial(req.getValor());
        usuarioRepository.save(usuario);

        return ResponseEntity.ok(nuevoPeso);
    }
}