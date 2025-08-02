package com.seusanimes.service;

import com.seusanimes.model.Anime;
import com.seusanimes.model.Categoria;
import com.seusanimes.repository.AnimeRepository;
import com.seusanimes.repository.CategoriaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AnimeExternalService {

    private final RestTemplate restTemplate;
    private final AnimeRepository animeRepository;
    private final CategoriaRepository categoriaRepository;

    @Value("${app.external.api.enabled:true}")
    private boolean isApiEnabled;

    public AnimeExternalService(RestTemplate restTemplate, AnimeRepository animeRepository, CategoriaRepository categoriaRepository) {
        this.restTemplate = restTemplate;
        this.animeRepository = animeRepository;
        this.categoriaRepository = categoriaRepository;
    }

    private final String EXTERNAL_API_URL = "https://api.jikan.moe/v4/anime";

    @SuppressWarnings("unchecked")
    private Anime processAndSaveAnimeData(Map<String, Object> animeData) {
        String titulo = (String) animeData.get("title");
        List<Anime> existingAnimes = animeRepository.findByTituloContainingIgnoreCase(titulo);
        if (!existingAnimes.isEmpty()) {
            System.out.println("Anime '" + titulo + "' já existe no banco de dados. Pulando importação.");
            return existingAnimes.get(0);
        }

        Anime anime = new Anime();
        anime.setTitulo(titulo);
        anime.setSinopse((String) animeData.get("synopsis"));
        anime.setEpisodios((Integer) animeData.get("episodes"));

        Map<String, Object> images = (Map<String, Object>) animeData.get("images");
        if (images != null && images.containsKey("jpg")) {
            Map<String, Object> jpgImages = (Map<String, Object>) images.get("jpg");
            if (jpgImages != null) {
                anime.setImagemUrl((String) jpgImages.get("image_url"));
            }
        }

        anime.setStatus((String) animeData.get("status"));

        Map<String, Object> airedMap = (Map<String, Object>) animeData.get("aired");
        if (airedMap != null && airedMap.containsKey("from")) {
            String airedString = (String) airedMap.get("from");
            if (airedString != null && !airedString.isEmpty()) {
                try {
                    LocalDate airedDate = LocalDate.parse(airedString.substring(0, 10));
                    anime.setAnoLancamento(airedDate);
                } catch (DateTimeParseException | StringIndexOutOfBoundsException e) {
                    try {
                        String[] parts = airedString.split(" ");
                        if (parts.length > 0) {
                            String lastPart = parts[parts.length - 1];
                            if (lastPart.matches("\\d{4}")) {
                                Integer year = Integer.parseInt(lastPart);
                                anime.setAnoLancamento(LocalDate.of(year, 1, 1));
                            } else {
                                anime.setAnoLancamento(null);
                            }
                        } else {
                            anime.setAnoLancamento(null);
                        }
                    } catch (NumberFormatException ex) {
                        anime.setAnoLancamento(null);
                    }
                }
            } else {
                anime.setAnoLancamento(null);
            }
        } else {
            anime.setAnoLancamento(null);
        }

        List<Map<String, String>> genres = (List<Map<String, String>>) animeData.get("genres");
        if (genres != null) {
            for (Map<String, String> genreMap : genres) {
                String categoryName = genreMap.get("name");
                if (categoryName != null && !categoryName.isEmpty()) {
                    Categoria categoria = categoriaRepository.findByNomeIgnoreCase(categoryName)
                            .orElseGet(() -> categoriaRepository.save(new Categoria(categoryName)));
                    anime.addCategoria(categoria);
                }
            }
        }

        return animeRepository.save(anime);
    }

    @SuppressWarnings("unchecked")
    public Optional<Anime> buscarESalvarAnime(String titulo) {
        if (!isApiEnabled) {
            System.err.println("Importação da API externa desabilitada. Recusando requisição.");
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "A importação de dados da API externa está desabilitada.");
        }

        try {
            List<Anime> existingAnimes = animeRepository.findByTituloContainingIgnoreCase(titulo);
            if (!existingAnimes.isEmpty()) {
                System.out.println("Anime '" + titulo + "' já existe no banco de dados. Retornando existente.");
                return Optional.of(existingAnimes.get(0));
            }

            String searchUrl = EXTERNAL_API_URL + "?q=" + titulo + "&sfw";
            Map<String, Object> response = restTemplate.getForObject(searchUrl, Map.class);

            if (response != null && response.containsKey("data")) {
                List<Map<String, Object>> dataList = (List<Map<String, Object>>) response.get("data");

                if (!dataList.isEmpty()) {
                    Map<String, Object> animeData = dataList.get(0);
                    return Optional.of(processAndSaveAnimeData(animeData));
                }
            }
            return Optional.empty();
        } catch (HttpClientErrorException.NotFound e) {
            System.err.println("API externa retornou 404 para o título: " + titulo);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Anime não encontrado na API externa.", e);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao buscar ou salvar anime por título.", e);
        }
    }

    @SuppressWarnings("unchecked")
    public List<Anime> buscarESalvarAnimesPorAno(Integer ano) {
        if (!isApiEnabled) {
            System.err.println("Importação da API externa desabilitada. Recusando requisição.");
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "A importação de dados da API externa está desabilitada.");
        }

        List<Anime> savedAnimes = new ArrayList<>();
        try {
            String url = EXTERNAL_API_URL + "?start_date=" + ano + "-01-01&end_date=" + ano + "-12-31&sfw";
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response != null && response.containsKey("data")) {
                List<Map<String, Object>> dataList = (List<Map<String, Object>>) response.get("data");

                for (Map<String, Object> animeData : dataList) {
                    try {
                        Anime savedAnime = processAndSaveAnimeData(animeData);
                        savedAnimes.add(savedAnime);
                    } catch (Exception e) {
                        System.err.println("Erro ao processar e salvar anime (ID Jikan: " + animeData.get("mal_id") + ", Título: " + animeData.get("title") + "): " + e.getMessage());
                    }
                }
            }
        } catch (HttpClientErrorException e) {
            System.err.println("Erro HTTP ao buscar animes por ano " + ano + ": " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            throw new ResponseStatusException(e.getStatusCode(), "Erro ao buscar animes por ano na API externa.", e);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro inesperado ao buscar animes por ano.", e);
        }
        return savedAnimes;
    }
}