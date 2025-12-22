package com.aliaga.fittrack.controller;

import com.aliaga.fittrack.dto.RutinaRequest;
import com.aliaga.fittrack.entity.*;
import com.aliaga.fittrack.repository.RutinaRepository;
import com.aliaga.fittrack.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional; // <--- IMPORTANTE
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/rutinas")
@RequiredArgsConstructor
public class RutinaController {

    private final RutinaRepository rutinaRepository;
    private final UsuarioRepository usuarioRepository;

    // 1. OBTENER MIS RUTINAS
    @GetMapping
    public ResponseEntity<List<Rutina>> misRutinas() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(rutinaRepository.findByUsuarioEmail(email));
    }

    // 2. CREAR RUTINA NUEVA
    @PostMapping
    public ResponseEntity<Rutina> crearRutina(@RequestBody RutinaRequest req) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Rutina rutina = Rutina.builder()
                .nombre(req.getNombre())
                .descripcion(req.getDescripcion())
                .fechaCreacion(LocalDate.now())
                .usuario(usuario)
                .dias(new ArrayList<>())
                .build();

        if (req.getDias() != null) {
            for (RutinaRequest.DiaRequest diaReq : req.getDias()) {
                DiaRutina dia = DiaRutina.builder()
                        .nombre(diaReq.getNombre())
                        .rutina(rutina)
                        .ejercicios(new ArrayList<>())
                        .build();

                if (diaReq.getEjercicios() != null) {
                    for (RutinaRequest.EjercicioRequest ejReq : diaReq.getEjercicios()) {
                        Ejercicio ejercicio = Ejercicio.builder()
                                .nombre(ejReq.getNombre())
                                .seriesObjetivo(ejReq.getSeriesObjetivo())
                                .repeticionesObjetivo(ejReq.getRepeticionesObjetivo())
                                .notas(ejReq.getNotas())
                                .diaRutina(dia)
                                .build();
                        dia.getEjercicios().add(ejercicio);
                    }
                }
                rutina.getDias().add(dia);
            }
        }

        rutinaRepository.save(rutina);
        return ResponseEntity.ok(rutina);
    }

    // 3. ELIMINAR RUTINA
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarRutina(@PathVariable Long id) {
        rutinaRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
    
    // 4. ACTUALIZAR RUTINA
    @PutMapping("/{id}")
    @Transactional // <--- ESTO ASEGURA QUE EL BORRADO Y LA INSERCIÓN SEAN ATÓMICOS
    public ResponseEntity<Rutina> actualizarRutina(@PathVariable Long id, @RequestBody RutinaRequest req) {
        Rutina rutinaExistente = rutinaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rutina no encontrada"));

        // 1. Actualizar datos básicos
        rutinaExistente.setNombre(req.getNombre());
        rutinaExistente.setDescripcion(req.getDescripcion());

        // 2. Limpiar días antiguos
        // Gracias a orphanRemoval=true en la entidad, esto BORRA los registros de la BD
        rutinaExistente.getDias().clear();

        // 3. Agregar los nuevos días
        if (req.getDias() != null) {
            for (RutinaRequest.DiaRequest diaReq : req.getDias()) {
                DiaRutina dia = DiaRutina.builder()
                        .nombre(diaReq.getNombre())
                        .rutina(rutinaExistente) // Enlazamos con la rutina existente
                        .ejercicios(new ArrayList<>())
                        .build();

                if (diaReq.getEjercicios() != null) {
                    for (RutinaRequest.EjercicioRequest ejReq : diaReq.getEjercicios()) {
                        Ejercicio ejercicio = Ejercicio.builder()
                                .nombre(ejReq.getNombre())
                                .seriesObjetivo(ejReq.getSeriesObjetivo())
                                .repeticionesObjetivo(ejReq.getRepeticionesObjetivo())
                                .notas(ejReq.getNotas())
                                .diaRutina(dia) // Enlazamos con el día nuevo
                                .build();
                        dia.getEjercicios().add(ejercicio);
                    }
                }
                rutinaExistente.getDias().add(dia);
            }
        }

        // 4. Guardar los cambios
        Rutina actualizada = rutinaRepository.save(rutinaExistente);
        return ResponseEntity.ok(actualizada);
    }
}