package com.aliaga.fittrack.repository;

import com.aliaga.fittrack.entity.HistorialEntrenamiento;
import com.aliaga.fittrack.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HistorialRepository extends JpaRepository<HistorialEntrenamiento, Long> {
    
    // Obtener todo el historial de un usuario ordenado por fecha (más reciente primero)
    List<HistorialEntrenamiento> findByUsuarioEmailOrderByFechaHoraDesc(String email);

    // CONSULTA AVANZADA: Buscar la última vez que hice un ejercicio específico
    @Query("""
        SELECT h FROM HistorialEntrenamiento h 
        JOIN h.ejerciciosRealizados e 
        WHERE h.usuario.email = :email 
        AND LOWER(e.nombreEjercicio) LIKE LOWER(CONCAT('%', :nombreEjercicio, '%'))
        ORDER BY h.fechaHora DESC
    """)
    List<HistorialEntrenamiento> encontrarHistorialPorEjercicio(@Param("email") String email, @Param("nombreEjercicio") String nombreEjercicio);
    void deleteByUsuario(Usuario usuario);
}