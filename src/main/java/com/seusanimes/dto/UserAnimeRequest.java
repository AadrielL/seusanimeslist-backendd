package com.seusanimes.dto;

import com.seusanimes.model.AnimeStatus; // Importação CORRETA do Enum

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAnimeRequest {
    private Long animeId;
    private AnimeStatus status; // Ex: WATCHING, COMPLETED, PLAN_TO_WATCH
    private Integer score; // Opcional
    private Integer episodesWatched; // Opcional
}