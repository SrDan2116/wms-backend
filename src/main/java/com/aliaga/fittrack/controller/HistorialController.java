package com.aliaga.fittrack.controller;

import com.aliaga.fittrack.dto.FinalizarEntrenoRequest;
import com.aliaga.fittrack.entity.*;
import com.aliaga.fittrack.repository.HistorialRepository;
import com.aliaga.fittrack.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/api/historial") 
@RequiredArgsConstructor
public class HistorialController {

    private final HistorialRepository historialRepository;
    private final UsuarioRepository usuarioRepository;

    // 1. OBTENER TODO EL HISTORIAL
    @GetMapping
    public ResponseEntity<List<HistorialEntrenamiento>> miHistorial() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(historialRepository.findByUsuarioEmailOrderByFechaHoraDesc(email));
    }

    // 2. GUARDAR UN ENTRENAMIENTO TERMINADO
    @PostMapping
    public ResponseEntity<HistorialEntrenamiento> guardarEntreno(@RequestBody FinalizarEntrenoRequest req) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow();
        
        HistorialEntrenamiento historial = HistorialEntrenamiento.builder()
                .usuario(usuario)
                .nombreRutina(req.getNombreRutina())
                .notasGenerales(req.getNotasGenerales())
                .fechaHora(req.getFechaHora() != null ? req.getFechaHora() : LocalDateTime.now())
                .ejerciciosRealizados(new ArrayList<>())
                .build();

        if (req.getEjercicios() != null) {
            for (FinalizarEntrenoRequest.EjercicioRealizadoDTO ejDto : req.getEjercicios()) {
                RegistroEjercicio regEj = RegistroEjercicio.builder()
                        .nombreEjercicio(ejDto.getNombre())
                        .historial(historial)
                        .series(new ArrayList<>())
                        .build();

                if (ejDto.getSeries() != null) {
                    AtomicInteger contadorSerie = new AtomicInteger(1);
                    for (FinalizarEntrenoRequest.SerieDTO serieDto : ejDto.getSeries()) {
                        Serie serie = Serie.builder()
                                .numeroSerie(contadorSerie.getAndIncrement())
                                .peso(serieDto.getPeso())
                                .repeticiones(serieDto.getRepeticiones())
                                .rpe(serieDto.getRpe())
                                .registroEjercicio(regEj)
                                .build();
                        regEj.getSeries().add(serie);
                    }
                }
                historial.getEjerciciosRealizados().add(regEj);
            }
        }

        historialRepository.save(historial);
        return ResponseEntity.ok(historial);
    }

    // 3. NUEVO: ELIMINAR HISTORIAL (AGREGAR ESTO)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarHistorial(@PathVariable Long id) {
        historialRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}