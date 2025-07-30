package com.seusanimes.dto;

import com.seusanimes.model.AnimeStatus; // Importação CORRETA do Enum

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAnimeResponse {
    private Long id;
    private Long animeId;
    private String animeName;
    private String animeGenre;
    private Integer animeReleaseYear;

    private String animeSynopsis; // Campo adicionado
    private String animeImageUrl; // Campo adicionado
    
    private AnimeStatus status;
    private Integer score;
    private Integer episodesWatched;
}