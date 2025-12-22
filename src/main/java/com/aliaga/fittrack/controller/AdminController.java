package com.aliaga.fittrack.controller;

import com.aliaga.fittrack.dto.SuspensionRequest;
import com.aliaga.fittrack.entity.SolicitudEliminacion;
import com.aliaga.fittrack.entity.Usuario;
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
    
    // Inyectamos repositorios hijos para limpieza manual si es necesario
    private final RutinaRepository rutinaRepository;
    private final HistorialRepository historialRepository;
    private final PesoRepository pesoRepository;
    private final PasswordTokenRepository tokenRepository;

    // 1. VER TODOS LOS USUARIOS
    @GetMapping("/users")
    public ResponseEntity<List<Usuario>> getAllUsers() {
        return ResponseEntity.ok(usuarioRepository.findAll());
    }

    // 2. VER SOLICITUDES DE ELIMINACIÓN
    @GetMapping("/deletion-requests")
    public ResponseEntity<List<SolicitudEliminacion>> getDeletionRequests() {
        return ResponseEntity.ok(solicitudRepository.findAll());
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

    // 5. ELIMINAR CUENTA DEFINITIVAMENTE (Limpia todo)
    @DeleteMapping("/user/{id}")
    @Transactional
    public ResponseEntity<Void> deleteUserPermanently(@PathVariable Long id) {
        Usuario user = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // 1. Limpiar dependencias (Si no usaste CascadeType.REMOVE en Usuario)
        // Por seguridad, limpiamos todo explícitamente
        tokenRepository.findByUsuario(user).ifPresent(tokenRepository::delete);
        solicitudRepository.deleteByUsuarioId(id);
        
        // Asumiendo que rutinas/historial tienen FKs que saltarían si no los borramos:
        // (Si tus entidades tienen orphanRemoval/cascade, esto puede no ser necesario, 
        // pero es más seguro hacerlo así en el controller de Admin)
        // rutinaRepository.deleteByUsuario(user); // Implementar si hace falta
        // historialRepository.deleteByUsuario(user);
        // pesoRepository.deleteByUsuario(user);

        // 2. Borrar Usuario
        usuarioRepository.delete(user);
        
        return ResponseEntity.noContent().build();
    }
}