package com.rventura.abscinema.movies;

import java.util.List;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record MovieToSave(
    @Size(min = 1, max = 400) String title,
    @Size(min = 1, max = 400) String director,
    @Size(min = 1, max = 400) String mainCast,
    List<String> genres,
    @Positive int runtime_minutes,
    LocalDate releaseDate,
    @Size(min = 1, max = 1000) String synopsis
// String posterUrl,
) {
}
