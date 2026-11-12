import { Component, inject } from "@angular/core";
import { Route, Router } from "@angular/router";
import { MovieApi } from "../../movie";

@Component({
  selector: 'app-movie-list',
  templateUrl: './movie-list.html',
  styleUrl: './movie-list.css',
})
class MovieListPage {
  private router = inject(Router);
  private movieStore = inject(MovieApi);

  goToAddMoviePage() {
    this.router.navigateByUrl('/new/movie');
  }

  goToEditMoviePage(id: number) {
    this.router.navigateByUrl(`/edit/movie/${id}`);
  }
}

export const MoviesListPageRoute: Route = {
  path: 'movies',
  component: MovieListPage,
  title: 'Movies'
}
