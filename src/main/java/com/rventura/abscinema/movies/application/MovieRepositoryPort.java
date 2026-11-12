package com.rventura.abscinema.movies.application;

import java.util.List;
import com.rventura.abscinema.movies.domain.Movie;
import com.rventura.abscinema.movies.domain.MovieToSave;
import com.rventura.abscinema.shared.exceptions.InvalidInputOnSaveException;

public interface MovieRepositoryPort {
    Movie create(MovieToSave movie) throws InvalidInputOnSaveException;

    List<Movie> findAll();
}
