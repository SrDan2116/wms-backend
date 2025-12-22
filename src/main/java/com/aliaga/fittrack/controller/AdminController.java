package com.aliaga.fittrack.controller;

import com.aliaga.fittrack.dto.SuspensionRequest;
import com.aliaga.fittrack.entity.SolicitudEliminacion;
import com.aliaga.fittrack.entity.Usuario;
import com.aliaga.fittrack.enums.EstadoSolicitud;
import com.aliaga.fittrack.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UsuarioRepository usuarioRepository;
    private final SolicitudEliminacionRepository solicitudRepository;
    
    private final PasswordTokenRepository tokenRepository;
    // Otros repositorios opcionales si usas borrado manual...

    // 1. VER TODOS LOS USUARIOS
    @GetMapping("/users")
    public ResponseEntity<List<Usuario>> getAllUsers() {
        return ResponseEntity.ok(usuarioRepository.findAll());
    }

    // 2. VER SOLICITUDES (Devuelve todas, pendientes primero)
    @GetMapping("/deletion-requests")
    public ResponseEntity<List<SolicitudEliminacion>> getDeletionRequests() {
        // Podrías usar findByEstado(PENDIENTE) si solo quieres las nuevas
        return ResponseEntity.ok(solicitudRepository.findAll());
    }

    // --- NUEVO: CONTADOR PARA BADGE DE CAMPANA ---
    @GetMapping("/notifications-count")
    public ResponseEntity<Long> getPendingCount() {
        return ResponseEntity.ok(solicitudRepository.countByEstado(EstadoSolicitud.PENDIENTE));
    }

    // --- NUEVO: MARCAR COMO ATENDIDO (Sin borrar usuario) ---
    @PutMapping("/request/{id}/mark-handled")
    public ResponseEntity<Void> markAsHandled(@PathVariable Long id) {
        SolicitudEliminacion sol = solicitudRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));
        sol.setEstado(EstadoSolicitud.ATENDIDO);
        solicitudRepository.save(sol);
        return ResponseEntity.ok().build();
    }

    // 3. SUSPENDER USUARIO
    @PostMapping("/suspend")
    public ResponseEntity<Usuario> suspendUser(@RequestBody SuspensionRequest req) {
        Usuario user = usuarioRepository.findById(req.getUserId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        user.setSuspended(true);
        user.setSuspensionReason(req.getReason());

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

    // 4. LEVANTAR SUSPENSIÓN
    @PostMapping("/unsuspend/{id}")
    public ResponseEntity<Usuario> unsuspendUser(@PathVariable Long id) {
        Usuario user = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        user.setSuspended(false);
        user.setSuspensionReason(null);
        user.setSuspensionEndsAt(null);

        return ResponseEntity.ok(usuarioRepository.save(user));
    }

    // 5. ELIMINAR CUENTA DEFINITIVAMENTE
    @DeleteMapping("/user/{id}")
    @Transactional
    public ResponseEntity<Void> deleteUserPermanently(@PathVariable Long id) {
        Usuario user = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        tokenRepository.findByUsuario(user).ifPresent(tokenRepository::delete);
        solicitudRepository.deleteByUsuarioId(id);
        
        usuarioRepository.delete(user);
        
        return ResponseEntity.noContent().build();
    }
}