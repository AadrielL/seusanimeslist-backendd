package com.seusanimes.service;

import com.seusanimes.model.Anime;
import com.seusanimes.repository.AnimeRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDate; // IMPORTANTE: Certifique-se de que esta importação existe
import java.util.List;
import java.util.Optional;

@Service
public class AnimeService {

    private final AnimeRepository animeRepository;

    public AnimeService(AnimeRepository animeRepository) {
        this.animeRepository = animeRepository;
    }

    public List<Anime> getAllAnimes() {
        return animeRepository.findAll();
    }

    public Optional<Anime> getAnimeById(Long id) {
        return animeRepository.findById(id);
    }

    public List<Anime> findByTituloContainingIgnoreCase(String titulo) {
        return animeRepository.findByTituloContainingIgnoreCase(titulo);
    }

    // CORREÇÃO AQUI:
    // Adaptação para construir o intervalo de datas necessário
    // para o método findByAnoLancamentoBetween do repositório.
    public List<Anime> findByAnoLancamento(Integer ano) {
        if (ano == null) {
            // Decide o comportamento quando o ano é nulo.
            // Pode retornar uma lista vazia, todos os animes, ou lançar uma exceção.
            return List.of(); // Retorna uma lista vazia como padrão.
        }
        // Cria as datas de início e fim para o ano fornecido
        LocalDate startDate = LocalDate.of(ano, 1, 1);
        LocalDate endDate = LocalDate.of(ano, 12, 31);
        return animeRepository.findByAnoLancamentoBetween(startDate, endDate);

        // Se você usou a @Query no AnimeRepository (Opção Alternativa acima):
        // return animeRepository.findByAno(ano);
    }

    public List<Anime> getAnimesByGenre(String genre) {
        // Implementação para buscar por gênero, se aplicável (ex: adicione um método ao repositório)
        // Por enquanto, placeholder:
        return List.of();
    }

    public Anime criarAnime(Anime anime) {
        return animeRepository.save(anime);
    }

    public Optional<Anime> atualizarAnime(Long id, Anime animeAtualizado) {
        return animeRepository.findById(id)
                .map(anime -> {
                    anime.setTitulo(animeAtualizado.getTitulo());
                    anime.setSinopse(animeAtualizado.getSinopse()); // Exemplo de atualização de outros campos
                    anime.setEpisodios(animeAtualizado.getEpisodios());
                    anime.setImagemUrl(animeAtualizado.getImagemUrl());
                    anime.setStatus(animeAtualizado.getStatus());
                    anime.setAnoLancamento(animeAtualizado.getAnoLancamento());
                    // Cuidado com categorias: se for um relacionamento @ManyToMany, você precisará gerenciar
                    // a adição/remoção de categorias existentes ou novas.
                    // Para simplicidade, assumindo que você já tem a lógica de categorias em addCategoria
                    if (animeAtualizado.getCategorias() != null) {
                        anime.getCategorias().clear(); // Limpa as categorias antigas
                        animeAtualizado.getCategorias().forEach(anime::addCategoria); // Adiciona as novas
                    }
                    return animeRepository.save(anime);
                });
    }

    public void deletarAnime(Long id) {
        animeRepository.deleteById(id);
    }
}