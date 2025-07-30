package com.seusanimes.controller; // PACOTE CORRETO PARA O SEUSANIMELIST

import com.seusanimes.model.Anime;
import com.seusanimes.service.AnimeService; // Assumindo que AnimeService conterá a lógica
import com.seusanimes.service.AnimeExternalService; // Necessário para a importação da API externa

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@RestController // MUITO IMPORTANTE: Garante que esta classe é um REST Controller
@RequestMapping("/api/animes") // Caminho base para os endpoints de animes
@CrossOrigin(origins = "http://localhost:5173") // Permite requisições do seu frontend React
public class AnimeController {

    private final AnimeService animeService;
    private final AnimeExternalService animeExternalService; // Para a funcionalidade de importação

    // Construtor para injeção de dependências
    public AnimeController(AnimeService animeService, AnimeExternalService animeExternalService) {
        this.animeService = animeService;
        this.animeExternalService = animeExternalService;
    }

    // =========================================================================
    // ENDPOINTS DO SEUSANIMELIST (AJUSTADOS PARA USAR O NOVO AnimeService)
    // =========================================================================

    /**
     * Endpoint para buscar todos os animes ou filtrar por título/ano.
     * GET /api/animes?titulo={titulo}&ano={ano}
     * GET /api/animes
     * (Mescla do buscarAnimes da api-animes e getAllAnimes do seusanimelist)
     */
    @GetMapping
    public ResponseEntity<List<Anime>> getAllAnimes(
            @RequestParam(required = false) String titulo,
            @RequestParam(required = false) Integer ano) {

        if (titulo != null && !titulo.isEmpty()) {
            List<Anime> animes = animeService.findByTituloContainingIgnoreCase(titulo);
            return ResponseEntity.ok(animes);
        } else if (ano != null) {
            List<Anime> animes = animeService.findByAnoLancamento(ano);
            return ResponseEntity.ok(animes);
        } else {
            List<Anime> animes = animeService.getAllAnimes(); // Busca todos se não houver filtros
            return ResponseEntity.ok(animes);
        }
    }

    /**
     * Endpoint para buscar um anime pelo ID.
     * GET /api/animes/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Anime> getAnimeById(@PathVariable Long id) {
        Optional<Anime> anime = animeService.getAnimeById(id);
        return anime.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Endpoint para pesquisar animes por nome (já coberto pelo GET / com 'titulo').
     * Manter para compatibilidade se o frontend usa '/search'.
     * GET /api/animes/search?q={query}
     */
    @GetMapping("/search")
    public ResponseEntity<List<Anime>> searchAnimes(@RequestParam String q) {
        List<Anime> animes = animeService.findByTituloContainingIgnoreCase(q); // Reutilizando o método de busca por título
        return ResponseEntity.ok(animes);
    }

    /**
     * Endpoint para buscar animes por gênero.
     * GET /api/animes/genre/{genero}
     */
    @GetMapping("/genre/{genre}")
    public ResponseEntity<List<Anime>> getAnimesByGenre(@PathVariable String genre) {
        List<Anime> animes = animeService.getAnimesByGenre(genre);
        return ResponseEntity.ok(animes);
    }

    // =========================================================================
    // ENDPOINTS DE IMPORTAÇÃO E CRUD DO MINHAAPIANIMES (AGORA NO SEUSANIMELIST)
    // =========================================================================

    /**
     * POST /api/animes/buscar-e-salvar?titulo=... - Busca um anime na API externa e salva (um por um)
     * Delega para o AnimeExternalService
     */
    @PostMapping("/buscar-e-salvar")
    public ResponseEntity<Anime> buscarESalvarAnime(@RequestParam String titulo) {
        try {
            Optional<Anime> animeSalvo = animeExternalService.buscarESalvarAnime(titulo);
            return animeSalvo.map(anime -> ResponseEntity.status(HttpStatus.CREATED).body(anime))
                             .orElse(ResponseEntity.notFound().build());
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * POST /api/animes/buscar-e-salvar-por-ano?ano=... - Busca VÁRIOS animes na API externa por ano e salva
     * Delega para o AnimeExternalService
     */
    @PostMapping("/buscar-e-salvar-por-ano")
    public ResponseEntity<List<Anime>> buscarESalvarAnimesPorAno(@RequestParam Integer ano) {
        try {
            List<Anime> animesSalvos = animeExternalService.buscarESalvarAnimesPorAno(ano);
            if (animesSalvos.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(animesSalvos);
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(animesSalvos);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * POST /api/animes - Cria um novo anime manualmente
     * Delega para o AnimeService
     */
    @PostMapping
    public ResponseEntity<Anime> criarAnime(@RequestBody Anime anime) {
        Anime novoAnime = animeService.criarAnime(anime); // Novo método no service
        return ResponseEntity.status(HttpStatus.CREATED).body(novoAnime);
    }

    /**
     * PUT /api/animes/{id} - Atualiza um anime existente
     * Delega para o AnimeService
     */
    @PutMapping("/{id}")
    public ResponseEntity<Anime> atualizarAnime(@PathVariable Long id, @RequestBody Anime animeAtualizado) {
        Optional<Anime> updatedAnime = animeService.atualizarAnime(id, animeAtualizado); // Novo método no service
        return updatedAnime.map(ResponseEntity::ok)
                           .orElse(ResponseEntity.notFound().build());
    }

    /**
     * DELETE /api/animes/{id} - Deleta um anime
     * Delega para o AnimeService
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletarAnime(@PathVariable Long id) {
        animeService.deletarAnime(id); // Novo método no service
    }
}