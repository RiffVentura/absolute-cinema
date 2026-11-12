import { Component, inject, linkedSignal } from '@angular/core';
import { ActivatedRoute, Route, Router, RouterLink } from '@angular/router';
import { Movie, MovieApi, MovieToSave } from '../../movie';
import { form, FormField, FormRoot, maxLength, min, required } from '@angular/forms/signals';
import { toSubmissionErrors } from '../../../shared/form-errors';

interface MovieFields {
  title: string;
  director: string;
  mainCast: string;
  genres: string;
  runtimeMinutes: number | null;
  releaseDate: string;
  synopsis: string;
}

function toMovieFields(movie: Movie | undefined): MovieFields {
  return {
    title: movie?.title ?? '',
    director: movie?.director ?? '',
    mainCast: movie?.mainCast ?? '',
    genres: movie?.genres.join(', ') ?? '',
    runtimeMinutes: movie?.runtimeMinutes ?? null,
    releaseDate: movie?.releaseDate ?? '',
    synopsis: movie?.synopsis ?? '',
  };
}

function toMovieToSave(fields: MovieFields): MovieToSave {
  return {
    title: fields.title,
    director: fields.director,
    mainCast: fields.mainCast || null,
    genres: fields.genres
      .split(',')
      .map((genre) => genre.trim())
      .filter((genre) => genre.length > 0),
    runtimeMinutes: fields.runtimeMinutes,
    releaseDate: fields.releaseDate || null,
    synopsis: fields.synopsis || null,
  };
}

@Component({
  selector: 'app-movie-edit',
  templateUrl: './edit-movie.html',
  styleUrl: './edit-movie.css',
  imports: [RouterLink, FormField, FormRoot],
})
class EditMoviePage {
  private movieApi = inject(MovieApi);
  private router = inject(Router);
  private movieId = Number(inject(ActivatedRoute).snapshot.paramMap.get('id'));

  movie = this.movieApi.movie(() => this.movieId);

  private movieFieldsModel = linkedSignal(() =>
    toMovieFields(this.movie.hasValue() ? this.movie.value() : undefined),
  );

  movieFieldsForm = form(
    this.movieFieldsModel,
    (schemaPath) => {
      required(schemaPath.title, { message: 'Movie needs a title' });
      maxLength(schemaPath.title, 400, { message: 'Title is too long (400 characters max)' });
      required(schemaPath.director, { message: 'Movie needs a director' });
      maxLength(schemaPath.director, 400, { message: 'Director is too long (400 characters max)' });
      maxLength(schemaPath.mainCast, 1000, { message: 'Cast is too long (1000 characters max)' });
      min(schemaPath.runtimeMinutes, 1, { message: 'Runtime must be at least 1 minute' });
      maxLength(schemaPath.synopsis, 5000, {
        message: 'Synopsis is too long (5000 characters max)',
      });
    },
    {
      submission: {
        action: async (data) => {
          try {
            await this.movieApi.update(this.movieId, toMovieToSave(data().value()));
          } catch (e) {
            return toSubmissionErrors(e, data);
          }
          await this.router.navigateByUrl('/movies');
          return;
        },
      },
    },
  );
}

export const EditMoviePageRoute: Route = {
  path: 'edit/movie/:id',
  component: EditMoviePage,
  title: 'Edit movie',
};
