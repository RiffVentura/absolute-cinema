package com.rventura.abscinema.movies.application;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Service;
import com.rventura.abscinema.movies.domain.Movie;
import com.rventura.abscinema.movies.domain.MovieToSave;

@Service
public class MovieService {
    private final MovieRepositoryPort movieRepository;

    public MovieService(MovieRepositoryPort movieRepository) {
        this.movieRepository = movieRepository;
    }

    public Movie create(MovieToSave movie) {
        return movieRepository.create(normalize(movie));
    }

    public List<Movie> findAll() {
        return movieRepository.findAll();
    }

    public Optional<Movie> findById(long id) {
        return movieRepository.findById(id);
    }

    public Optional<Movie> update(long id, MovieToSave movie) {
        return movieRepository.update(id, normalize(movie));
    }

    private MovieToSave normalize(MovieToSave movie) {
        List<String> genres = movie.genres() == null
            ? List.of()
            : movie.genres().stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(genre -> !genre.isEmpty())
                .distinct()
                .toList();

        return new MovieToSave(
            movie.title().trim(),
            movie.director().trim(),
            blankToNull(movie.mainCast()),
            genres,
            movie.runtimeMinutes(),
            movie.releaseDate(),
            blankToNull(movie.synopsis()));
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
