package com.rventura.abscinema.movies.domain;

import java.time.LocalDate;
import java.util.List;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record MovieToSave(
    @NotBlank(message = "Movie needs a title")
    @Size(max = 400, message = "Title is too long (400 characters max)")
    String title,

    @NotBlank(message = "Movie needs a director")
    @Size(max = 400, message = "Director is too long (400 characters max)")
    String director,

    @Size(max = 1000, message = "Cast is too long (1000 characters max)")
    String mainCast,

    @Size(max = 20, message = "Too many genres (20 max)")
    List<
        @NotBlank(message = "Genres can't be empty")
        @Size(max = 50, message = "Each genre is limited to 50 characters") String> genres,

    @Positive(message = "Runtime must be at least 1 minute")
    Integer runtimeMinutes,

    LocalDate releaseDate,

    @Size(max = 5000, message = "Synopsis is too long (5000 characters max)")
    String synopsis) {
}
