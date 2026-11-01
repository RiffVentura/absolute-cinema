import { HttpClient, httpResource } from "@angular/common/http";
import { inject, Service } from "@angular/core";

export interface Movie {
  id: string;
  title: string;
  releaseDate: string;
  posterUrl: string;
}

interface NewMovie {
  title: string;
  description: string;
  releaseDate: Date;
}


@Service()
export class MovieApi {
  private http = inject(HttpClient);
  private _movies = httpResource<Movie[]>(
    () => `/internal/movies/`,
    { defaultValue: [] },
  );

  readonly movies = this._movies.asReadonly();

  async create(newMovie: NewMovie) {
    this.http.post<Movie>('/internal/movies/', newMovie).subscribe((movie) => {
      console.log(movie);
    });
  }

}


