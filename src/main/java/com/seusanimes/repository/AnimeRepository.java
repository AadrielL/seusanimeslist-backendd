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

    // 1. PAGINAÇÃO GERAL: JpaRepository já fornece:
    // Page<Anime> findAll(Pageable pageable);

    // 2. BUSCA POR TÍTULO: OK. Não paginamos, pois a busca é específica.
    List<Anime> findByTituloContainingIgnoreCase(String titulo);

    // 3. 🚀 CORREÇÃO CRUCIAL: PAGINAÇÃO NA BUSCA POR ANO
    // Um ano pode ter milhares de animes. O retorno deve ser Page, não List.
    @Query("SELECT a FROM Anime a WHERE a.anoLancamento = :ano")
    Page<Anime> findByAnoLancamento(@Param("ano") Integer ano, Pageable pageable);
    // OBS: Removi a função YEAR() do JPQL, pois seu modelo de dados Anime já tem 'anoLancamento' como Integer (ano),
    // o que é mais performático. Se 'anoLancamento' for um campo DATE no seu modelo, a função YEAR() está correta.

    // 4. BUSCA POR CATEGORIA: OK. Mantenho a List, mas poderia ser Page se for um gênero popular.
    List<Anime> findByCategorias_NomeContainingIgnoreCase(String categoriaNome);

    // 5. BUSCA POR TÍTULO EXATO: OK (usado no service para evitar duplicidade antes de salvar).
    Optional<Anime> findByTitulo(String titulo);

    // 6. BUSCA POR CATEGORIA COM PAGINAÇÃO (Opcional, mas altamente recomendado para gêneros populares):
    Page<Anime> findByCategorias_NomeContainingIgnoreCase(String categoriaNome, Pageable pageable);
}