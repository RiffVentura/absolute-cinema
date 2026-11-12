import { HttpClient, httpResource } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { firstValueFrom } from 'rxjs';

export interface Movie {
  id: number;
  title: string;
  director: string;
  mainCast: string | null;
  genres: string[];
  runtimeMinutes: number | null;
  releaseDate: string | null;
  synopsis: string | null;
  posterUrl: string | null;
  createdAt: string;
}

export interface MovieToSave {
  title: string;
  director: string;
  mainCast?: string | null;
  genres?: string[];
  runtimeMinutes?: number | null;
  releaseDate?: string | null;
  synopsis?: string | null;
}

@Service()
export class MovieApi {
  private http = inject(HttpClient);
  private _movies = httpResource<Movie[]>(() => `/internal/movies/`, { defaultValue: [] });

  readonly movies = this._movies.asReadonly();

  async create(newMovie: MovieToSave): Promise<Movie> {
    const movie = await firstValueFrom(this.http.post<Movie>('/internal/movies/', newMovie));
    this._movies.reload();
    return movie;
  }
}
