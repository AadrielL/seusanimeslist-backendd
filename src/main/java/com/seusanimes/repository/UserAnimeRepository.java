package com.seusanimes.repository;

import com.seusanimes.model.UserAnime;
import com.seusanimes.model.User;
import com.seusanimes.model.Anime;
import com.seusanimes.model.AnimeStatus; // Importação correta do Enum

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserAnimeRepository extends JpaRepository<UserAnime, Long> {

    List<UserAnime> findByUser(User user);

    List<UserAnime> findByUserAndStatus(User user, AnimeStatus status);

    Optional<UserAnime> findByUserAndAnime(User user, Anime anime);

    long countByUserAndStatus(User user, AnimeStatus status);

    // Método para validar que uma entrada UserAnime pertence a um usuário específico.
    Optional<UserAnime> findByUserIdAndId(Long userId, Long id);
    @Query("SELECT ua.status, COUNT(ua) FROM UserAnime ua WHERE ua.user.username = :username GROUP BY ua.status")
    List<Object[]> countAnimesByStatusForUser(@Param("username") String username);
}