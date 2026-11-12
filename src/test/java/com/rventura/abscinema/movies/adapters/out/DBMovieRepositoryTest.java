package com.rventura.abscinema.movies.adapters.out;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import com.rventura.abscinema.movies.domain.Movie;
import com.rventura.abscinema.movies.domain.MovieToSave;
import com.rventura.abscinema.shared.exceptions.InUseException;
import com.rventura.abscinema.shared.exceptions.InvalidInputOnSaveException;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import(DBMovieRepository.class)
class DBMovieRepositoryTest {

    @Autowired
    private DBMovieRepository repository;

    @Autowired
    private NamedParameterJdbcTemplate jdbc;

    @Test
    void deleteRemovesTheMovieAndReportsWhetherItExisted() {
        Movie created = repository.create(new MovieToSave("Alien", "Ridley Scott", null, List.of(), null, null, null));

        assertThat(repository.delete(created.id())).isTrue();
        assertThat(repository.findById(created.id())).isEmpty();
        assertThat(repository.delete(created.id())).isFalse();
    }

    @Test
    void deleteOfMovieWithSessionsThrowsInUse() {
        Movie created = repository.create(new MovieToSave("Alien", "Ridley Scott", null, List.of(), null, null, null));
        Long hallId = jdbc.queryForObject(
            "INSERT INTO halls (alias) VALUES ('Hall 1') RETURNING id", new MapSqlParameterSource(), Long.class);
        jdbc.update("""
            INSERT INTO movie_sessions (movie_id, hall_id, timeslot_start, timeslot_end)
            VALUES (:movieId, :hallId, TIMESTAMP '2026-11-12 20:00', TIMESTAMP '2026-11-12 22:00')
            """, new MapSqlParameterSource("movieId", created.id()).addValue("hallId", hallId));

        assertThatThrownBy(() -> repository.delete(created.id())).isInstanceOf(InUseException.class);
    }

    @Test
    void createInsertsAllFieldsAndReturnsGeneratedColumns() {
        Movie created = repository.create(new MovieToSave(
            "The Matrix", "The Wachowskis", "Keanu Reeves", List.of("Sci-Fi", "Action"), 136,
            LocalDate.of(1999, 3, 31), "A hacker learns the truth."));

        assertThat(created.id()).isPositive();
        assertThat(created.title()).isEqualTo("The Matrix");
        assertThat(created.director()).isEqualTo("The Wachowskis");
        assertThat(created.mainCast()).isEqualTo("Keanu Reeves");
        assertThat(created.genres()).containsExactly("Sci-Fi", "Action");
        assertThat(created.runtimeMinutes()).isEqualTo(136);
        assertThat(created.releaseDate()).isEqualTo(LocalDate.of(1999, 3, 31));
        assertThat(created.synopsis()).isEqualTo("A hacker learns the truth.");
        assertThat(created.posterUrl()).isNull();
        assertThat(created.createdAt()).isNotNull();
    }

    @Test
    void createAcceptsOnlyRequiredFields() {
        Movie created = repository.create(new MovieToSave(
            "Alien", "Ridley Scott", null, List.of(), null, null, null));

        assertThat(created.genres()).isEmpty();
        assertThat(created.runtimeMinutes()).isNull();
        assertThat(created.releaseDate()).isNull();
    }

    @Test
    void findAllReturnsNewestFirst() {
        Movie first = repository.create(new MovieToSave("Alien", "Ridley Scott", null, List.of(), null, null, null));
        Movie second = repository.create(new MovieToSave("Aliens", "James Cameron", null, List.of(), null, null, null));

        assertThat(repository.findAll()).containsSubsequence(second, first);
    }

    @Test
    void createTranslatesDatabaseRejectionIntoInvalidInputOnSave() {
        assertThatThrownBy(() -> repository.create(new MovieToSave(
            null, "Ridley Scott", null, List.of(), null, null, null)))
            .isInstanceOf(InvalidInputOnSaveException.class);
    }

    @Test
    void findByIdReturnsTheMovieOrEmpty() {
        Movie created = repository.create(new MovieToSave("Alien", "Ridley Scott", null, List.of("Horror"), null, null, null));

        assertThat(repository.findById(created.id())).contains(created);
        assertThat(repository.findById(-1)).isEmpty();
    }

    @Test
    void updateReplacesAllEditableFieldsAndKeepsTheRest() {
        Movie created = repository.create(new MovieToSave(
            "Alien", "Ridley Scott", "Sigourney Weaver", List.of("Horror"), 117, LocalDate.of(1979, 5, 25), "In space."));

        Movie updated = repository.update(created.id(), new MovieToSave(
            "Aliens", "James Cameron", null, List.of("Action", "Sci-Fi"), 137, LocalDate.of(1986, 7, 18), null))
            .orElseThrow();

        assertThat(updated).isEqualTo(new Movie(
            created.id(), "Aliens", "James Cameron", null, List.of("Action", "Sci-Fi"), 137,
            LocalDate.of(1986, 7, 18), null, created.posterUrl(), created.createdAt()));
        assertThat(repository.findById(created.id())).contains(updated);
    }

    @Test
    void updateOfMissingMovieReturnsEmpty() {
        assertThat(repository.update(-1, new MovieToSave("Alien", "Ridley Scott", null, List.of(), null, null, null)))
            .isEmpty();
    }

    @Test
    void updateTranslatesDatabaseRejectionIntoInvalidInputOnSave() {
        Movie created = repository.create(new MovieToSave("Alien", "Ridley Scott", null, List.of(), null, null, null));

        assertThatThrownBy(() -> repository.update(created.id(), new MovieToSave(
            "Alien", null, null, List.of(), null, null, null)))
            .isInstanceOf(InvalidInputOnSaveException.class);
    }
}
