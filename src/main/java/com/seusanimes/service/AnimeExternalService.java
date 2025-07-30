package com.seusanimes.service; // PACOTE CORRETO PARA O SEUSANIMELIST

import com.seusanimes.model.Anime; // NOVO PACOTE
import com.seusanimes.model.Categoria; // NOVO PACOTE
import com.seusanimes.repository.AnimeRepository; // NOVO PACOTE
import com.seusanimes.repository.CategoriaRepository; // NOVO PACOTE
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

    // Construtor para injeção de dependências
    public AnimeExternalService(RestTemplate restTemplate, AnimeRepository animeRepository, CategoriaRepository categoriaRepository) {
        this.restTemplate = restTemplate;
        this.animeRepository = animeRepository;
        this.categoriaRepository = categoriaRepository;
    }

    private final String EXTERNAL_API_URL = "https://api.jikan.moe/v4/anime";

    @SuppressWarnings("unchecked") // Suprime warnings de unchecked casts de Object para Map/List
    private Anime processAndSaveAnimeData(Map<String, Object> animeData) {
        String titulo = (String) animeData.get("title");
        // CORREÇÃO: findByTituloContainingIgnoreCase agora retorna List<Anime>
        // Verificamos se a lista não está vazia para saber se o anime já existe.
        List<Anime> existingAnimes = animeRepository.findByTituloContainingIgnoreCase(titulo);
        if (!existingAnimes.isEmpty()) { // Correção: usa isEmpty() para verificar se a lista tem elementos
            System.out.println("Anime '" + titulo + "' já existe no banco de dados. Pulando importação.");
            return existingAnimes.get(0); // Pega o primeiro anime encontrado, se houver
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
                    // Tenta parsear a data completa (ex: "2023-01-01T00:00:00+00:00")
                    LocalDate airedDate = LocalDate.parse(airedString.substring(0, 10)); // Pega só a parte YYYY-MM-DD
                    anime.setAnoLancamento(airedDate);
                } catch (DateTimeParseException | StringIndexOutOfBoundsException e) {
                    // Se falhar, tenta extrair só o ano
                    try {
                        String[] parts = airedString.split(" ");
                        if (parts.length > 0) {
                            String lastPart = parts[parts.length - 1];
                            if (lastPart.matches("\\d{4}")) { // Verifica se são 4 dígitos (um ano)
                                Integer year = Integer.parseInt(lastPart);
                                // Define o ano como 1 de janeiro daquele ano
                                anime.setAnoLancamento(LocalDate.of(year, 1, 1));
                            } else {
                                anime.setAnoLancamento(null); // Não conseguiu extrair um ano válido
                            }
                        } else {
                            anime.setAnoLancamento(null); // String vazia ou sem partes
                        }
                    } catch (NumberFormatException ex) {
                        anime.setAnoLancamento(null); // Falha ao converter para número
                    }
                }
            } else {
                anime.setAnoLancamento(null); // airedString é nula ou vazia
            }
        } else {
            anime.setAnoLancamento(null); // airedMap ou 'from' é nulo
        }

        List<Map<String, String>> genres = (List<Map<String, String>>) animeData.get("genres");
        if (genres != null) {
            for (Map<String, String> genreMap : genres) {
                String categoryName = genreMap.get("name");
                if (categoryName != null && !categoryName.isEmpty()) {
                    Categoria categoria = categoriaRepository.findByNomeIgnoreCase(categoryName)
                        .orElseGet(() -> categoriaRepository.save(new Categoria(categoryName)));
                    anime.addCategoria(categoria); // Garanta que Anime.java tem este método
                }
            }
        }

        return animeRepository.save(anime);
    }

    @SuppressWarnings("unchecked")
    // CORRIGIDO: O tipo de retorno agora é Optional<Anime> e o método usa List<Anime> do repo corretamente.
    public Optional<Anime> buscarESalvarAnime(String titulo) {
        try {
            // Usa findByTituloContainingIgnoreCase que retorna List<Anime>
            List<Anime> existingAnimes = animeRepository.findByTituloContainingIgnoreCase(titulo);
            if (!existingAnimes.isEmpty()) { // Verifica se a lista não está vazia
                System.out.println("Anime '" + titulo + "' já existe no banco de dados. Retornando existente.");
                return Optional.of(existingAnimes.get(0)); // Retorna o primeiro anime encontrado encapsulado em Optional
            }

            String searchUrl = EXTERNAL_API_URL + "?q=" + titulo + "&sfw"; // Adicionado &sfw para conteúdo seguro
            Map<String, Object> response = restTemplate.getForObject(searchUrl, Map.class);

            if (response != null && response.containsKey("data")) {
                List<Map<String, Object>> dataList = (List<Map<String, Object>>) response.get("data");

                if (!dataList.isEmpty()) {
                    Map<String, Object> animeData = dataList.get(0); // Pega o primeiro resultado da API externa
                    return Optional.of(processAndSaveAnimeData(animeData)); // Processa, salva e retorna em um Optional
                }
            }
            return Optional.empty(); // Nenhum anime encontrado na API externa
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
        List<Anime> savedAnimes = new ArrayList<>();
        try {
            // A API Jikan.moe pode ter limites de página. Para um ano, pode haver muitas páginas.
            // Esta implementação pega apenas a primeira página. Para mais, seria necessário paginar.
            String url = EXTERNAL_API_URL + "?start_date=" + ano + "-01-01&end_date=" + ano + "-12-31&sfw"; // Adicionado &sfw
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response != null && response.containsKey("data")) {
                List<Map<String, Object>> dataList = (List<Map<String, Object>>) response.get("data");

                for (Map<String, Object> animeData : dataList) {
                    try {
                        Anime savedAnime = processAndSaveAnimeData(animeData);
                        savedAnimes.add(savedAnime);
                    } catch (Exception e) {
                        System.err.println("Erro ao processar e salvar anime (ID Jikan: " + animeData.get("mal_id") + ", Título: " + animeData.get("title") + "): " + e.getMessage());
                        // Continua processando os outros animes mesmo com erro em um
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