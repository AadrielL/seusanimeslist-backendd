package com.seusanimes.controller;

import com.seusanimes.model.Anime;
import com.seusanimes.service.AnimeService;
import com.seusanimes.service.AnimeExternalService;

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
            List<Anime> animes = animeService.getAllAnimes();
            return ResponseEntity.ok(animes);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Anime> getAnimeById(@PathVariable Long id) {
        Optional<Anime> anime = animeService.getAnimeById(id);
        return anime.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<List<Anime>> searchAnimes(@RequestParam String q) {
        List<Anime> animes = animeService.findByTituloContainingIgnoreCase(q);
        return ResponseEntity.ok(animes);
    }

    @GetMapping("/genre/{genre}")
    public ResponseEntity<List<Anime>> getAnimesByGenre(@PathVariable String genre) {
        List<Anime> animes = animeService.getAnimesByGenre(genre);
        return ResponseEntity.ok(animes);
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