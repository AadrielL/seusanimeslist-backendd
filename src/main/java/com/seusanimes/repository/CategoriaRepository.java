package com.seusanimes.repository;

import com.seusanimes.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional; // Don't forget this import for Optional

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    Optional<Categoria> findByNomeIgnoreCase(String nome);
}