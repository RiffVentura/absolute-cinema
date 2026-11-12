package com.rventura.abscinema.movies.domain;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public record Movie(
    Long id,
    String title,
    String director,
    String mainCast,
    List<String> genres,
    Integer runtimeMinutes,
    LocalDate releaseDate,
    String synopsis,
    String posterUrl,
    OffsetDateTime createdAt) {
}
