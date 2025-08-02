package com.seusanimes.service;

import com.seusanimes.model.Anime;
import com.seusanimes.repository.AnimeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
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

    public List<Anime> getAllAnimes() {
        return animeRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }

    public Optional<Anime> getAnimeById(Long id) {
        return animeRepository.findById(id);
    }

    public List<Anime> findByTituloContainingIgnoreCase(String titulo) {
        return animeRepository.findByTituloContainingIgnoreCase(titulo);
    }

    public List<Anime> findByAnoLancamento(Integer ano) {
        return animeRepository.findByAnoLancamento(ano);
    }

    public List<Anime> findByCategorias_NomeContainingIgnoreCase(String categoria) {
        return animeRepository.findByCategorias_NomeContainingIgnoreCase(categoria);
    }

    public Anime criarAnime(Anime anime) {
        return animeRepository.save(anime);
    }

    public Optional<Anime> atualizarAnime(Long id, Anime animeAtualizado) {
        return animeRepository.findById(id)
                .map(anime -> {
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

    public void deletarAnime(Long id) {
        animeRepository.deleteById(id);
    }
}