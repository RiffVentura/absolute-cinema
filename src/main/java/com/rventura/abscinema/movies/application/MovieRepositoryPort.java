package com.rventura.abscinema.movies.application;

import java.util.List;
import java.util.Optional;
import com.rventura.abscinema.movies.domain.Movie;
import com.rventura.abscinema.movies.domain.MovieToSave;
import com.rventura.abscinema.shared.exceptions.InUseException;
import com.rventura.abscinema.shared.exceptions.InvalidInputOnSaveException;

public interface MovieRepositoryPort {
    Movie create(MovieToSave movie) throws InvalidInputOnSaveException;

    List<Movie> findAll();

    Optional<Movie> findById(long id);

    Optional<Movie> update(long id, MovieToSave movie) throws InvalidInputOnSaveException;

    boolean delete(long id) throws InUseException;
}
