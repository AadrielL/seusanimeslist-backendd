// ARQUIVO: src/main/java/com/seusanimes/repository/AnimeRepository.java
package com.seusanimes.repository;

import com.seusanimes.model.Anime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnimeRepository extends JpaRepository<Anime, Long> {

    List<Anime> findByTituloContainingIgnoreCase(String titulo);

    // Método corrigido para buscar por ano
    @Query("SELECT a FROM Anime a WHERE YEAR(a.anoLancamento) = :ano")
    List<Anime> findByAnoLancamento(@Param("ano") Integer ano);

    List<Anime> findByCategorias_NomeContainingIgnoreCase(String categoriaNome);

    Optional<Anime> findByTitulo(String titulo);
}