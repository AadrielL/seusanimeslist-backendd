package com.seusanimes.controller;

import com.seusanimes.dto.UserAnimeRequest;
import com.seusanimes.dto.UserAnimeResponse;
import com.seusanimes.model.User;
import com.seusanimes.model.UserAnime;
import com.seusanimes.model.AnimeStatus;
import com.seusanimes.model.Categoria;

import com.seusanimes.service.AnimeService;
import com.seusanimes.service.UserAnimeService;
import com.seusanimes.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user-animes")
public class UserAnimeController {

    private final UserAnimeService userAnimeService;
    private final UserService userService;
    private final AnimeService animeService;

    @Autowired
    public UserAnimeController(UserAnimeService userAnimeService,
                               UserService userService,
                               AnimeService animeService) {
        this.userAnimeService = userAnimeService;
        this.userService = userService;
        this.animeService = animeService;
    }

    private Long getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            System.out.println("UserAnimeController: Usuário não autenticado ou anônimo.");
            throw new RuntimeException("Usuário não autenticado.");
        }

        String username;
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }

        System.out.println("UserAnimeController: Usuário autenticado: " + username);

        Optional<User> userOptional = userService.getUserByUsername(username);
        if (!userOptional.isPresent()) {
            System.out.println("UserAnimeController: Usuário '" + username + "' não encontrado no banco de dados após autenticação!");
            throw new RuntimeException("Usuário autenticado não encontrado no banco de dados!");
        }
        Long userId = userOptional.get().getId();
        System.out.println("UserAnimeController: userId autenticado: " + userId);
        return userId;
    }

    private String getAuthenticatedUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            throw new RuntimeException("Usuário não autenticado.");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else {
            return principal.toString();
        }
    }


    private UserAnimeResponse convertToDto(UserAnime userAnime) {
        UserAnimeResponse dto = new UserAnimeResponse();
        dto.setId(userAnime.getId());
        dto.setAnimeId(userAnime.getAnime().getId());
        dto.setAnimeName(userAnime.getAnime().getTitulo());
        
        if (userAnime.getAnime().getCategorias() != null && !userAnime.getAnime().getCategorias().isEmpty()) {
            dto.setAnimeGenre(userAnime.getAnime().getCategorias().stream()
                                         .map(Categoria::getNome)
                                         .collect(Collectors.joining(", ")));
        } else {
            dto.setAnimeGenre(null); 
        }

        if (userAnime.getAnime().getAnoLancamento() != null) {
            dto.setAnimeReleaseYear(userAnime.getAnime().getAnoLancamento().getYear());
        } else {
            dto.setAnimeReleaseYear(null);
        }

        dto.setAnimeSynopsis(userAnime.getAnime().getSinopse());
        dto.setAnimeImageUrl(userAnime.getAnime().getImagemUrl());
        
        dto.setStatus(userAnime.getStatus());
        dto.setScore(userAnime.getScore());
        dto.setEpisodesWatched(userAnime.getEpisodesWatched());
        return dto;
    }

    @PostMapping
    public ResponseEntity<UserAnimeResponse> addAnimeToUserList(@RequestBody UserAnimeRequest request) {
        System.out.println("UserAnimeController: Acessando addAnimeToUserList (POST).");
        try {
            Long userId = getAuthenticatedUserId();

            if (request.getAnimeId() == null || request.getStatus() == null) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            UserAnime userAnime = userAnimeService.addOrUpdateUserAnime(
                    userId, request.getAnimeId(), request.getStatus());

            UserAnimeResponse responseDto = convertToDto(userAnime);
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);

        } catch (RuntimeException e) {
            System.err.println("Erro ao adicionar/atualizar anime na lista do usuário: " + e.getMessage());
            if (e.getMessage().contains("não encontrado")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PutMapping("/{userAnimeId}")
    public ResponseEntity<UserAnimeResponse> updateAnimeInUserList(
                @PathVariable Long userAnimeId,
                @RequestBody UserAnimeRequest request) {
        System.out.println("UserAnimeController: Acessando updateAnimeInUserList (PUT).");
        try {
            Long userId = getAuthenticatedUserId();

            UserAnime userAnime = userAnimeService.updateDetailsUserAnime(
                    userId, userAnimeId, request.getStatus(), request.getScore(), request.getEpisodesWatched());

            UserAnimeResponse responseDto = convertToDto(userAnime);
            return ResponseEntity.ok(responseDto);
        } catch (RuntimeException e) {
            System.err.println("Erro ao atualizar detalhes do anime na lista do usuário: " + e.getMessage());
            if (e.getMessage().contains("não encontrada ou não pertence ao usuário")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @DeleteMapping("/{userAnimeId}")
    public ResponseEntity<Void> deleteAnimeFromUserList(@PathVariable Long userAnimeId) {
        System.out.println("UserAnimeController: Acessando deleteAnimeFromUserList (DELETE).");
        Long userId = getAuthenticatedUserId();

        boolean deleted = userAnimeService.deleteUserAnime(userId, userAnimeId);
        
        if (deleted) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping
    public ResponseEntity<List<UserAnimeResponse>> getUserAnimeList(
                @RequestParam(required = false) AnimeStatus status) {
        System.out.println("UserAnimeController: Acessando getUserAnimeList (GET).");
        Long userId = getAuthenticatedUserId();

        List<UserAnime> userAnimes;
        if (status != null) {
            userAnimes = userAnimeService.getUserAnimeListByStatus(userId, status);
        } else {        
            userAnimes = userAnimeService.getUserAnimeList(userId);
        }

        List<UserAnimeResponse> responses = userAnimes.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/status-counts")
    public ResponseEntity<Map<AnimeStatus, Long>> getAnimeStatusCounts() {
        System.out.println("UserAnimeController: Acessando getAnimeStatusCounts (GET).");
        String username = getAuthenticatedUsername();

        Map<AnimeStatus, Long> statusCounts = userAnimeService.getAnimeStatusCountsForUser(username);
        return ResponseEntity.ok(statusCounts);
    }

    @GetMapping("/count")
    public ResponseEntity<Long> countUserAnimesByStatus(
                @RequestParam AnimeStatus status) {
        System.out.println("UserAnimeController: Acessando countUserAnimesByStatus (GET).");
        Long userId = getAuthenticatedUserId();

        if (status == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        long count = userAnimeService.countUserAnimesByStatus(userId, status);
        return ResponseEntity.ok(count);
    }
}