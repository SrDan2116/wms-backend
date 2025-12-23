package com.aliaga.fittrack.repository;

import com.aliaga.fittrack.entity.Peso;
import com.aliaga.fittrack.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PesoRepository extends JpaRepository<Peso, Long> {
    
    List<Peso> findByUsuarioEmailOrderByFechaAsc(String email);
    void deleteByUsuario(Usuario usuario);
}