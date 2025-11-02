package com.seusanimes.controller;

import com.seusanimes.model.Anime;
import com.seusanimes.service.AnimeService;
import com.seusanimes.service.AnimeExternalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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

    @Autowired
    public AnimeController(AnimeService animeService, AnimeExternalService animeExternalService) {
        this.animeService = animeService;
        this.animeExternalService = animeExternalService;
    }

    // 1. BUSCA GERAL (PAGINADA) - Corrigido para chamar .findAll(pageable)
    @GetMapping
    public ResponseEntity<Page<Anime>> getAllAnimes(
            @PageableDefault(size = 10, sort = "titulo") Pageable pageable) {

        Page<Anime> animesPage = animeService.findAll(pageable); // 👈 Corrigido: Usando findAll
        return ResponseEntity.ok(animesPage);
    }

    // ---------------------------------------------------------------------
    // Endpoints de Busca por Filtro
    // ---------------------------------------------------------------------

    // 2. BUSCA POR TÍTULO
    @GetMapping("/search")
    public ResponseEntity<List<Anime>> searchAnimes(@RequestParam String q) {
        List<Anime> animes = animeService.findByTituloContainingIgnoreCase(q);
        return animes.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(animes);
    }

    // 3. BUSCA POR GÊNERO/CATEGORIA
    @GetMapping("/genre/{genre}")
    public ResponseEntity<List<Anime>> getAnimesByGenre(@PathVariable String genre) {
        List<Anime> animes = animeService.findAnimesByGenre(genre);
        return animes.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(animes);
    }

    // 4. BUSCA POR ANO DE LANÇAMENTO (PAGINADA E CORRIGIDA)
    @GetMapping("/ano/{ano}") // Ajuste para PathVariable para consistência
    public ResponseEntity<Page<Anime>> getAnimesByAnoLancamento(
            @PathVariable Integer ano,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {

        // Linha 84 Corrigida: Passando o Pageable
        Page<Anime> animesPage = animeService.findByAnoLancamento(ano, pageable);
        return ResponseEntity.ok(animesPage);
    }

    // ---------------------------------------------------------------------
    // Endpoints CRUD e External Service
    // ---------------------------------------------------------------------

    // 5. BUSCA POR ID - Corrigido para chamar .findById(id)
    @GetMapping("/{id}")
    public ResponseEntity<Anime> getAnimeById(@PathVariable Long id) {
        Optional<Anime> anime = animeService.findById(id); // 👈 Corrigido: Usando findById
        return anime.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

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

    // 6. CRIAR NOVO ANIME - Corrigido para chamar .save(anime)
    @PostMapping
    public ResponseEntity<Anime> criarAnime(@RequestBody Anime anime) {
        Anime novoAnime = animeService.save(anime); // 👈 Corrigido: Usando save
        return ResponseEntity.status(HttpStatus.CREATED).body(novoAnime);
    }

    // 7. ATUALIZAR ANIME
    @PutMapping("/{id}")
    public ResponseEntity<Anime> atualizarAnime(@PathVariable Long id, @RequestBody Anime animeAtualizado) {
        // Linha 139 Corrigida: Usando updateAnime
        Optional<Anime> updatedAnime = animeService.updateAnime(id, animeAtualizado);
        return updatedAnime.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 8. DELETAR ANIME - Corrigido para chamar .deleteById(id)
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletarAnime(@PathVariable Long id) {
        animeService.deleteById(id); // 👈 Corrigido: Usando deleteById
    }
}