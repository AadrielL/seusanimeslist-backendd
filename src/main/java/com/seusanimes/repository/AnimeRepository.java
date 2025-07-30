package com.seusanimes.repository;

import com.seusanimes.model.Anime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate; // IMPORTANTE: Certifique-se de que esta importação existe
import java.util.List;
import java.util.Optional;

@Repository
public interface AnimeRepository extends JpaRepository<Anime, Long> {

    // Método para buscar animes por título (já estava correto para List)
    List<Anime> findByTituloContainingIgnoreCase(String titulo);

  
    // O campo 'anoLancamento' na entidade Anime é LocalDate.
    // Para buscar por ano (que é um Integer), precisamos de um método que lide com o LocalDate.
    // A melhor forma é buscar por um intervalo de datas.
    List<Anime> findByAnoLancamentoBetween(LocalDate startDate, LocalDate endDate);

    // OPÇÃO ALTERNATIVA (se você realmente quiser passar apenas o Integer do ano e tiver o Anime como LocalDate)
    // Se você *precisar* de um método que receba apenas um Integer,
    // você teria que usar uma @Query JPQL para extrair o ano da data:
    // @Query("SELECT a FROM Anime a WHERE YEAR(a.anoLancamento) = :ano")
    // List<Anime> findByAno(@Param("ano") Integer ano); // E você precisaria de @Param
}