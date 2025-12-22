package com.aliaga.fittrack.repository;

import com.aliaga.fittrack.entity.SolicitudEliminacion;
import com.aliaga.fittrack.enums.EstadoSolicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SolicitudEliminacionRepository extends JpaRepository<SolicitudEliminacion, Long> {

    boolean existsByUsuarioEmail(String email);
 
    void deleteByUsuarioId(Long usuarioId);

    // Para el badge de la campana
    long countByEstado(EstadoSolicitud estado);

    // Para ver listas filtradas
    List<SolicitudEliminacion> findByEstado(EstadoSolicitud estado);
}