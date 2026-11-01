package com.rventura.abscinema.movies;

import java.security.Principal;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.ui.Model;

@RestController
@RequestMapping("/internal/movies")
public class MoviesController {
    @GetMapping("/")
    public ResponseEntity<Movie[]> movies() {
        Movie[] movies = {
                new Movie("1", "Spider man", "url1", "05/06"),
                new Movie("2", "matrix", "url2", "05/06"),
                new Movie("3", "StarWARS", "url1", "05/06"),
        };

        return ResponseEntity.ok(movies);
    }

    @PostMapping("/")
    public ResponseEntity<Movie> addNewMovie() {

        return ResponseEntity.ok(new Movie("3", "StarWARS", "url1", "05/06"));

    }
}
