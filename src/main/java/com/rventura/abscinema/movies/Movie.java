package com.rventura.abscinema.movies;

public record Movie(
    String id,
    String title,
    String posterUrl,
    String releaseDate) {
}
