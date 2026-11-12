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
  private moviesResource = httpResource<Movie[]>(() => `/internal/movies/`, { defaultValue: [] });

  readonly movies = this.moviesResource.asReadonly();

  async create(newMovie: MovieToSave): Promise<Movie> {
    const movie = await firstValueFrom(this.http.post<Movie>('/internal/movies/', newMovie));
    this.moviesResource.reload();
    return movie;
  }

  movie(id: () => number) {
    return httpResource<Movie>(() => `/internal/movies/${id()}`);
  }

  async update(id: number, changedMovie: MovieToSave): Promise<Movie> {
    const movie = await firstValueFrom(
      this.http.put<Movie>(`/internal/movies/${id}`, changedMovie),
    );
    this.moviesResource.reload();
    return movie;
  }

  async delete(id: number): Promise<void> {
    await firstValueFrom(this.http.delete<void>(`/internal/movies/${id}`));
    this.moviesResource.reload();
  }
}
