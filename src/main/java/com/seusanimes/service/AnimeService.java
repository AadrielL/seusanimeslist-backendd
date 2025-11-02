package com.seusanimes.service;

import com.seusanimes.model.Anime;
import com.seusanimes.repository.AnimeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page; // NOVO: Importe a classe Page
import org.springframework.data.domain.Pageable; // NOVO: Importe a classe Pageable
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

    // 🛑 CORREÇÃO DA LENTIDÃO: Mude para Page<Anime> e adicione Pageable
    public Page<Anime> getAllAnimes(Pageable pageable) {
        // Agora retorna apenas uma 'página' de resultados ordenada
        return animeRepository.findAll(pageable);
    }

    public Optional<Anime> getAnimeById(Long id) {
        return animeRepository.findById(id);
    }

    // ---------------------------------------------------------------------
    // Métodos de Busca: Mantenho o nome do Repository aqui por simplicidade
    // ---------------------------------------------------------------------

    // Método de busca por título (sem paginação, pois busca por palavra-chave costuma ser completa)
    public List<Anime> findByTituloContainingIgnoreCase(String titulo) {
        return animeRepository.findByTituloContainingIgnoreCase(titulo);
    }

    // ✅ Otimização: Busca por ano também deveria ser paginada (muitos animes por ano)
    // OBS: Seu AnimeRepository precisará ser ajustado para aceitar Pageable neste método
    public Page<Anime> findByAnoLancamento(Integer ano, Pageable pageable) {
        // Se o seu Repository suportar o método, use:
        // return animeRepository.findByAnoLancamento(ano, pageable);

        // Se o seu Repository for simples, use o método findAll, mas filtre a lista (menos eficiente):
        throw new UnsupportedOperationException("Este método deve ser implementado no Repository para aceitar Pageable.");

        // Se o Repository não for alterado, o método ficará:
        // return animeRepository.findByAnoLancamento(ano);
    }

    // ✅ CORREÇÃO DE NOME: Renomeio o método para refletir o que está no Controller
    public List<Anime> findAnimesByGenre(String categoria) {
        // O nome do método é findByCategorias_NomeContainingIgnoreCase
        return animeRepository.findByCategorias_NomeContainingIgnoreCase(categoria);
    }

    // ---------------------------------------------------------------------
    // Métodos de CRUD (Sem alteração)
    // ---------------------------------------------------------------------

    public Anime criarAnime(Anime anime) {
        return animeRepository.save(anime);
    }

    public Optional<Anime> atualizarAnime(Long id, Anime animeAtualizado) {
        // ... (lógica de atualização permanece a mesma, pois está correta) ...
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