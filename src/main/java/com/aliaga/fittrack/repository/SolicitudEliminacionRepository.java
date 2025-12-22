package com.aliaga.fittrack.repository;

import com.aliaga.fittrack.entity.SolicitudEliminacion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SolicitudEliminacionRepository extends JpaRepository<SolicitudEliminacion, Long> {

    boolean existsByUsuarioEmail(String email);
 
    void deleteByUsuarioId(Long usuarioId);
}