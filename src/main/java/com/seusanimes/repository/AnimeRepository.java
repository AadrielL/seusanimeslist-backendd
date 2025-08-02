package com.seusanimes.repository;

import com.seusanimes.model.Anime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnimeRepository extends JpaRepository<Anime, Long> {

    List<Anime> findByTituloContainingIgnoreCase(String titulo);

    List<Anime> findByAnoLancamento(Integer ano);
    
    List<Anime> findByCategorias_NomeContainingIgnoreCase(String categoriaNome);

    Optional<Anime> findByTitulo(String titulo);
}