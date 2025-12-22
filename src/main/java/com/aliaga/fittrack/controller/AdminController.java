package com.aliaga.fittrack.controller;

import com.aliaga.fittrack.dto.SuspensionRequest;
import com.aliaga.fittrack.entity.Usuario;
import com.aliaga.fittrack.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UsuarioRepository usuarioRepository;

    // 1. VER TODOS LOS USUARIOS (Para ver lastLogin y estado)
    @GetMapping("/users")
    public ResponseEntity<List<Usuario>> getAllUsers() {
        return ResponseEntity.ok(usuarioRepository.findAll());
    }

    // 2. SUSPENDER USUARIO
    @PostMapping("/suspend")
    public ResponseEntity<Usuario> suspendUser(@RequestBody SuspensionRequest req) {
        Usuario user = usuarioRepository.findById(req.getUserId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        user.setSuspended(true);
        user.setSuspensionReason(req.getReason());

        // Calcular fecha fin
        LocalDateTime endDate = LocalDateTime.now();
        switch (req.getDurationUnit().toUpperCase()) {
            case "HOURS" -> endDate = endDate.plusHours(req.getDurationValue());
            case "DAYS" -> endDate = endDate.plusDays(req.getDurationValue());
            case "WEEKS" -> endDate = endDate.plusWeeks(req.getDurationValue());
            case "MONTHS" -> endDate = endDate.plusMonths(req.getDurationValue());
            case "PERMANENT" -> endDate = endDate.plusYears(99); 
        }
        user.setSuspensionEndsAt(endDate);

        return ResponseEntity.ok(usuarioRepository.save(user));
    }

    // 3. LEVANTAR SUSPENSIÓN MANUALMENTE
    @PostMapping("/unsuspend/{id}")
    public ResponseEntity<Usuario> unsuspendUser(@PathVariable Long id) {
        Usuario user = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        user.setSuspended(false);
        user.setSuspensionReason(null);
        user.setSuspensionEndsAt(null);

        return ResponseEntity.ok(usuarioRepository.save(user));
    }
}