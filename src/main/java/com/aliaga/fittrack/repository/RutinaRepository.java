package com.aliaga.fittrack.repository;

import com.aliaga.fittrack.entity.Rutina;
import com.aliaga.fittrack.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RutinaRepository extends JpaRepository<Rutina, Long> {
    List<Rutina> findByUsuarioEmail(String email);

    void deleteByUsuario(Usuario usuario);
}