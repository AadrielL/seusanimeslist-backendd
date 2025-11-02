package com.seusanimes.repository;

import com.seusanimes.model.Anime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnimeRepository extends JpaRepository<Anime, Long> {

    // 1. PAGINAÇÃO NA BUSCA POR ANO (CORRIGIDA)
    @Query("SELECT a FROM Anime a WHERE a.anoLancamento = :ano")
    Page<Anime> findByAnoLancamento(@Param("ano") Integer ano, Pageable pageable);

    // 2. BUSCA POR TÍTULO
    List<Anime> findByTituloContainingIgnoreCase(String titulo);

    // 3. BUSCA POR CATEGORIA
    List<Anime> findByCategorias_NomeContainingIgnoreCase(String categoriaNome);

    // 4. BUSCA POR TÍTULO EXATO
    Optional<Anime> findByTitulo(String titulo);
}