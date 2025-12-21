package com.aliaga.fittrack.repository;

import com.aliaga.fittrack.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Buscar usuario por email (Necesario para el Login)
    Optional<Usuario> findByEmail(String email);

    // Verificar si el email ya existe (Necesario para el Registro)
    boolean existsByEmail(String email);
}