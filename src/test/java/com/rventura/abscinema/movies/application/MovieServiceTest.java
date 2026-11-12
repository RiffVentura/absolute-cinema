package com.rventura.abscinema.movies.application;

import static org.assertj.core.api.Assertions.assertThat;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.rventura.abscinema.movies.domain.Movie;
import com.rventura.abscinema.movies.domain.MovieToSave;

class MovieServiceTest {
    private InMemoryMovieRepository repository;
    private MovieService service;

    @BeforeEach
    void setUp() {
        repository = new InMemoryMovieRepository();
        service = new MovieService(repository);
    }

    @Test
    void createPersistsMovieAndAssignsId() {
        Movie created = service.create(new MovieToSave(
            "The Matrix", "The Wachowskis", "Keanu Reeves", List.of("Sci-Fi"), 136,
            LocalDate.of(1999, 3, 31), "A hacker learns the truth."));

        assertThat(created.id()).isNotNull();
        assertThat(created.title()).isEqualTo("The Matrix");
        assertThat(repository.findAll()).containsExactly(created);
    }

    @Test
    void createTrimsTextAndTurnsBlankOptionalFieldsIntoNull() {
        Movie created = service.create(new MovieToSave(
            "  Alien ", " Ridley Scott ", "   ", null, null, null, ""));

        assertThat(created.title()).isEqualTo("Alien");
        assertThat(created.director()).isEqualTo("Ridley Scott");
        assertThat(created.mainCast()).isNull();
        assertThat(created.synopsis()).isNull();
        assertThat(created.genres()).isEmpty();
    }

    @Test
    void createCleansUpGenres() {
        Movie created = service.create(new MovieToSave(
            "Alien", "Ridley Scott", null, Arrays.asList(" Horror", "Sci-Fi", "Horror", " ", null),
            null, null, null));

        assertThat(created.genres()).containsExactly("Horror", "Sci-Fi");
    }
}
