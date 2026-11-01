
import { Routes } from "@angular/router";
import { MoviesListPageRoute } from "./pages/movie-list/movie-list";
import { AddMoviePageRoute } from "./pages/add-movie/add-movie";

export const moviesPages: Routes = [
  MoviesListPageRoute,
  AddMoviePageRoute,
];
