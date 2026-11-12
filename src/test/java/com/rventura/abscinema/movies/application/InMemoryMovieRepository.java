package com.rventura.abscinema.movies.application;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import com.rventura.abscinema.movies.domain.Movie;
import com.rventura.abscinema.movies.domain.MovieToSave;
import com.rventura.abscinema.shared.exceptions.InvalidInputOnSaveException;

public class InMemoryMovieRepository implements MovieRepositoryPort {
    private final Map<Long, Movie> movies = new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);

    @Override
    public Movie create(MovieToSave movie) {
        if (movie.title() == null || movie.director() == null) {
            throw new InvalidInputOnSaveException(new IllegalArgumentException("title and director are NOT NULL"));
        }
        Movie created = new Movie(
            nextId.getAndIncrement(),
            movie.title(),
            movie.director(),
            movie.mainCast(),
            List.copyOf(movie.genres()),
            movie.runtimeMinutes(),
            movie.releaseDate(),
            movie.synopsis(),
            null,
            OffsetDateTime.now());
        movies.put(created.id(), created);
        return created;
    }

    @Override
    public List<Movie> findAll() {
        List<Movie> all = new ArrayList<>(movies.values());
        all.sort(Comparator.comparing(Movie::id).reversed());
        return all;
    }

    @Override
    public Optional<Movie> findById(long id) {
        return Optional.ofNullable(movies.get(id));
    }

    @Override
    public Optional<Movie> update(long id, MovieToSave movie) {
        if (movie.title() == null || movie.director() == null) {
            throw new InvalidInputOnSaveException(new IllegalArgumentException("title and director are NOT NULL"));
        }
        return Optional.ofNullable(movies.computeIfPresent(id, (key, existing) -> new Movie(
            existing.id(),
            movie.title(),
            movie.director(),
            movie.mainCast(),
            List.copyOf(movie.genres()),
            movie.runtimeMinutes(),
            movie.releaseDate(),
            movie.synopsis(),
            existing.posterUrl(),
            existing.createdAt())));
    }

    @Override
    public boolean delete(long id) {
        return movies.remove(id) != null;
    }
}
