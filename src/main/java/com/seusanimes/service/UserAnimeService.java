package com.seusanimes.service;

import com.seusanimes.model.User;
import com.seusanimes.model.Anime;
import com.seusanimes.model.UserAnime;
import com.seusanimes.model.AnimeStatus; // Importação do Enum

import com.seusanimes.repository.UserAnimeRepository;
import com.seusanimes.repository.UserRepository; // Se não estiver importado
import com.seusanimes.repository.AnimeRepository; // Se não estiver importado

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Para operações transacionais

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class UserAnimeService {

    private final UserAnimeRepository userAnimeRepository;
    private final UserRepository userRepository; // Injetar se precisar buscar User aqui
    private final AnimeRepository animeRepository; // Injetar se precisar buscar Anime aqui

    @Autowired
    public UserAnimeService(UserAnimeRepository userAnimeRepository,
                            UserRepository userRepository,
                            AnimeRepository animeRepository) {
        this.userAnimeRepository = userAnimeRepository;
        this.userRepository = userRepository;
        this.animeRepository = animeRepository;
    }

    @Transactional // Garante que a operação seja atômica
    public UserAnime addOrUpdateUserAnime(Long userId, Long animeId, AnimeStatus status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com ID: " + userId));
        Anime anime = animeRepository.findById(animeId)
                .orElseThrow(() -> new RuntimeException("Anime não encontrado com ID: " + animeId));

        Optional<UserAnime> existingUserAnime = userAnimeRepository.findByUserAndAnime(user, anime);

        UserAnime userAnime;
        if (existingUserAnime.isPresent()) {
            userAnime = existingUserAnime.get();
            userAnime.setStatus(status);
            // Você pode adicionar lógica para manter score/episodesWatched ou resetar
            // userAnime.setScore(0); // Exemplo: resetar score
            // userAnime.setEpisodesWatched(0); // Exemplo: resetar episódios
        } else {
            userAnime = new UserAnime();
            userAnime.setUser(user);
            userAnime.setAnime(anime);
            userAnime.setStatus(status);
            userAnime.setScore(0); // Valor inicial
            userAnime.setEpisodesWatched(0); // Valor inicial
        }
        return userAnimeRepository.save(userAnime);
    }

    @Transactional
    public UserAnime updateDetailsUserAnime(Long userId, Long userAnimeId, AnimeStatus status, Integer score, Integer episodesWatched) {
        UserAnime userAnime = userAnimeRepository.findByUserIdAndId(userId, userAnimeId)
                .orElseThrow(() -> new RuntimeException("Entrada de anime do usuário não encontrada ou não pertence ao usuário."));

        if (status != null) {
            userAnime.setStatus(status);
        }
        if (score != null) {
            userAnime.setScore(score);
        }
        if (episodesWatched != null) {
            userAnime.setEpisodesWatched(episodesWatched);
        }
        return userAnimeRepository.save(userAnime);
    }

    @Transactional
    public boolean deleteUserAnime(Long userId, Long userAnimeId) {
        Optional<UserAnime> userAnimeOptional = userAnimeRepository.findByUserIdAndId(userId, userAnimeId);
        if (userAnimeOptional.isPresent()) {
            userAnimeRepository.delete(userAnimeOptional.get());
            return true;
        }
        return false;
    }

    public List<UserAnime> getUserAnimeList(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com ID: " + userId));
        return userAnimeRepository.findByUser(user);
    }

    public List<UserAnime> getUserAnimeListByStatus(Long userId, AnimeStatus status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com ID: " + userId));
        return userAnimeRepository.findByUserAndStatus(user, status);
    }

    public long countUserAnimesByStatus(Long userId, AnimeStatus status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com ID: " + userId));
        return userAnimeRepository.countByUserAndStatus(user, status);
    }

    // NOVO MÉTODO para obter as contagens de status
    public Map<AnimeStatus, Long> getAnimeStatusCountsForUser(String username) {
        List<Object[]> results = userAnimeRepository.countAnimesByStatusForUser(username);
        Map<AnimeStatus, Long> statusCounts = new HashMap<>();
        for (Object[] result : results) {
            AnimeStatus status = (AnimeStatus) result[0];
            Long count = (Long) result[1];
            statusCounts.put(status, count);
        }
        return statusCounts;
    }
}