package com.seusanimes.service;

import com.seusanimes.model.Anime;
import com.seusanimes.repository.AnimeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AnimeService {

    private final AnimeRepository animeRepository;

    @Autowired
    public AnimeService(AnimeRepository animeRepository) {
        this.animeRepository = animeRepository;
    }

    // 1. BUSCA GERAL (PAGINADO)
    public Page<Anime> findAll(Pageable pageable) {
        return animeRepository.findAll(pageable);
    }

    // 2. BUSCA POR ID
    public Optional<Anime> findById(Long id) {
        return animeRepository.findById(id);
    }

    // 3. BUSCA POR ANO DE LANÇAMENTO (PAGINADO)
    public Page<Anime> findByAnoLancamento(Integer ano, Pageable pageable) {
        // Agora o Service chama o Repository com os dois argumentos
        return animeRepository.findByAnoLancamento(ano, pageable);
    }

    // 4. BUSCA POR TÍTULO
    public List<Anime> findByTituloContainingIgnoreCase(String titulo) {
        return animeRepository.findByTituloContainingIgnoreCase(titulo);
    }

    // 5. BUSCA POR GÊNERO/CATEGORIA
    public List<Anime> findAnimesByGenre(String categoria) {
        return animeRepository.findByCategorias_NomeContainingIgnoreCase(categoria);
    }

    // 6. SALVAR/ATUALIZAR
    public Anime save(Anime anime) {
        return animeRepository.save(anime);
    }

    // 7. ATUALIZAR (Lógica completa baseada no seu código)
    public Optional<Anime> updateAnime(Long id, Anime animeAtualizado) {
        return animeRepository.findById(id)
                .map(anime -> {
                    // Mapeamento dos campos
                    anime.setTitulo(animeAtualizado.getTitulo());
                    anime.setSinopse(animeAtualizado.getSinopse());
                    anime.setEpisodios(animeAtualizado.getEpisodios());
                    anime.setImagemUrl(animeAtualizado.getImagemUrl());
                    anime.setStatus(animeAtualizado.getStatus());
                    anime.setAnoLancamento(animeAtualizado.getAnoLancamento());
                    if (animeAtualizado.getCategorias() != null) {
                        anime.getCategorias().clear();
                        animeAtualizado.getCategorias().forEach(anime::addCategoria);
                    }
                    return animeRepository.save(anime);
                });
    }

    // 8. DELETAR POR ID
    public void deleteById(Long id) {
        animeRepository.deleteById(id);
    }
}