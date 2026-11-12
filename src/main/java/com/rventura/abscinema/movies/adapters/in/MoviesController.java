package com.rventura.abscinema.movies.adapters.in;

import java.net.URI;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.rventura.abscinema.movies.application.MovieService;
import com.rventura.abscinema.movies.domain.Movie;
import com.rventura.abscinema.movies.domain.MovieToSave;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/internal/movies")
public class MoviesController {
    private final MovieService movieService;

    public MoviesController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping("/")
    public List<Movie> movies() {
        return movieService.findAll();
    }

    @PostMapping(path = "/", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Movie> addNewMovie(@Valid @RequestBody MovieToSave newMovie) {
        Movie created = movieService.create(newMovie);
        return ResponseEntity.created(URI.create("/internal/movies/" + created.id())).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Movie> movie(@PathVariable long id) {
        return ResponseEntity.of(movieService.findById(id));
    }

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Movie> updateMovie(@PathVariable long id, @Valid @RequestBody MovieToSave movie) {
        return ResponseEntity.of(movieService.update(id, movie));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMovie(@PathVariable long id) {
        return movieService.delete(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
