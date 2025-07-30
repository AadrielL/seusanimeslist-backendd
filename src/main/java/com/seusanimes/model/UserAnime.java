package com.seusanimes.model;

import jakarta.persistence.*; // Use 'javax.persistence.*' se estiver em Spring Boot 2.x
import java.time.LocalDateTime;

@Entity
@Table(name = "user_animes")
public class UserAnime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anime_id", nullable = false)
    private Anime anime;

    @Enumerated(EnumType.STRING) // Importante para armazenar como String
    @Column(name = "status", nullable = false)
    private AnimeStatus status; // Tipo do Enum correto

    @Column(name = "score")
    private Integer score;

    @Column(name = "episodes_watched")
    private Integer episodesWatched;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    public UserAnime() {
    }

    // Getters e Setters (Certifique-se que estão presentes ou use Lombok com @Data na entidade)
    // Se você usa Lombok, pode remover manualmente estes getters/setters e deixar apenas @Data.
    // Estou incluindo-os aqui para garantir que o código funcione mesmo sem Lombok se ele for o problema.

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Anime getAnime() {
        return anime;
    }

    public void setAnime(Anime anime) {
        this.anime = anime;
    }

    public AnimeStatus getStatus() {
        return status;
    }

    public void setStatus(AnimeStatus status) {
        this.status = status;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Integer getEpisodesWatched() {
        return episodesWatched;
    }

    public void setEpisodesWatched(Integer episodesWatched) {
        this.episodesWatched = episodesWatched;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(LocalDateTime finishedAt) {
        this.finishedAt = finishedAt;
    }
}