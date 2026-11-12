import { maxLength, min, required, schema } from '@angular/forms/signals';
import { Movie, MovieToSave } from './movie';

export interface MovieFields {
  title: string;
  director: string;
  mainCast: string;
  genres: string;
  runtimeMinutes: number | null;
  releaseDate: string;
  synopsis: string;
}

export function toMovieFields(movie?: Movie): MovieFields {
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

export function toMovieToSave(fields: MovieFields): MovieToSave {
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

export const movieFieldsSchema = schema<MovieFields>((schemaPath) => {
  required(schemaPath.title, { message: 'Movie needs a title' });
  maxLength(schemaPath.title, 400, { message: 'Title is too long (400 characters max)' });
  required(schemaPath.director, { message: 'Movie needs a director' });
  maxLength(schemaPath.director, 400, { message: 'Director is too long (400 characters max)' });
  maxLength(schemaPath.mainCast, 1000, { message: 'Cast is too long (1000 characters max)' });
  min(schemaPath.runtimeMinutes, 1, { message: 'Runtime must be at least 1 minute' });
  maxLength(schemaPath.synopsis, 5000, { message: 'Synopsis is too long (5000 characters max)' });
});
