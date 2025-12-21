package com.aliaga.fittrack.repository;

import com.aliaga.fittrack.entity.Peso;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PesoRepository extends JpaRepository<Peso, Long> {
    // Buscar por usuario y ordenar por fecha (el más antiguo primero para el gráfico)
    List<Peso> findByUsuarioEmailOrderByFechaAsc(String email);
}