import { Component, inject, signal } from '@angular/core';
import { Route, Router, RouterLink } from '@angular/router';
import { MovieApi, MovieToSave } from '../../movie';
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
  selector: 'app-movie-add',
  templateUrl: './add-movie.html',
  styleUrl: './add-movie.css',
  imports: [RouterLink, FormField, FormRoot],
})
class AddMoviePage {
  private movieApi = inject(MovieApi);
  private router = inject(Router);

  private movieFieldsModel = signal<MovieFields>({
    title: '',
    director: '',
    mainCast: '',
    genres: '',
    runtimeMinutes: null,
    releaseDate: '',
    synopsis: '',
  });

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
            await this.movieApi.create(toMovieToSave(data().value()));
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

export const AddMoviePageRoute: Route = {
  path: 'new/movie',
  component: AddMoviePage,
  title: 'Add a new movie',
};
