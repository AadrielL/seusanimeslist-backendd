package com.seusanimes.controller;

import com.seusanimes.model.Anime;
import com.seusanimes.service.AnimeService;
import com.seusanimes.service.AnimeExternalService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/animes")
public class AnimeController {

    private final AnimeService animeService;
    private final AnimeExternalService animeExternalService;

    public AnimeController(AnimeService animeService, AnimeExternalService animeExternalService) {
        this.animeService = animeService;
        this.animeExternalService = animeExternalService;
    }

    // 🚀 NOVO ENDPOINT PRINCIPAL: PAGINAÇÃO E FILTROS!
    // GET /api/animes?page=0&size=20&sort=titulo,asc
    @GetMapping
    public ResponseEntity<Page<Anime>> listarAnimesPaginados(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            // Exemplo: ?sort=titulo,asc
            @RequestParam(defaultValue = "id,desc") String[] sort,
            // Filtros agora usam endpoints separados ou o endpoint de busca
            @RequestParam(required = false) String titulo,
            @RequestParam(required = false) Integer ano) {

        // Cria o objeto Sort e PageRequest
        Sort sorting = Sort.by(Sort.Direction.fromString(sort[1]), sort[0]);
        PageRequest pageRequest = PageRequest.of(page, size, sorting);

        // A lógica de filtros grandes deve ser isolada, mas mantemos o getAllAnimes para o caso geral

        // Se o título ou ano estiverem presentes, é melhor usar os endpoints de busca dedicados
        // Para o GET principal, focamos no carregamento paginado e geral

        // Se houver filtro por título, o usuário deveria usar /api/animes/search?q=...
        // Se houver filtro por ano, o usuário deveria usar /api/animes/ano?ano=...

        // Por enquanto, apenas chama o método paginado geral, resolvendo a lentidão
        Page<Anime> animesPage = animeService.getAllAnimes(pageRequest);

        return ResponseEntity.ok(animesPage);
    }

    // ---------------------------------------------------------------------
    // Endpoints de Busca por Filtro (mantidos separados para clareza)
    // ---------------------------------------------------------------------

    // Busca por Título (Melhorado para ser mais RESTful)
    // Mantido como List<Anime>
    @GetMapping("/search")
    public ResponseEntity<List<Anime>> searchAnimes(@RequestParam String q) {
        List<Anime> animes = animeService.findByTituloContainingIgnoreCase(q);
        return animes.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(animes);
    }

    // Busca por Gênero/Categoria (Corrigido para usar o método correto no Service)
    @GetMapping("/genre/{genre}")
    public ResponseEntity<List<Anime>> getAnimesByGenre(@PathVariable String genre) {
        // ✅ CORREÇÃO: Chama o método findAnimesByGenre, que agora existe no Service
        List<Anime> animes = animeService.findAnimesByGenre(genre);
        return animes.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(animes);
    }

    // Busca por Ano (Endpoint de busca separado)
    @GetMapping("/ano")
    public ResponseEntity<List<Anime>> getAnimesByAno(@RequestParam Integer ano) {
        // A busca por ano também deveria ser paginada, mas por enquanto, chamamos o método existente.
        List<Anime> animes = animeService.findByAnoLancamento(ano);
        return animes.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(animes);
    }

    // ---------------------------------------------------------------------
    // Endpoints CRUD e External Service (Sem alteração, pois estavam corretos)
    // ---------------------------------------------------------------------

    @GetMapping("/{id}")
    public ResponseEntity<Anime> getAnimeById(@PathVariable Long id) {
        Optional<Anime> anime = animeService.getAnimeById(id);
        return anime.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/buscar-e-salvar")
    public ResponseEntity<Anime> buscarESalvarAnime(@RequestParam String titulo) {
        // ... (lógica do external service)
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

    @PostMapping("/buscar-e-salvar-por-ano")
    public ResponseEntity<List<Anime>> buscarESalvarAnimesPorAno(@RequestParam Integer ano) {
        // ... (lógica do external service)
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

    @PostMapping
    public ResponseEntity<Anime> criarAnime(@RequestBody Anime anime) {
        Anime novoAnime = animeService.criarAnime(anime);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoAnime);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Anime> atualizarAnime(@PathVariable Long id, @RequestBody Anime animeAtualizado) {
        Optional<Anime> updatedAnime = animeService.atualizarAnime(id, animeAtualizado);
        return updatedAnime.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletarAnime(@PathVariable Long id) {
        animeService.deletarAnime(id);
    }
}