import { Component, inject, linkedSignal, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, Route, Router, RouterLink } from '@angular/router';
import { MovieApi } from '../../movie';
import { movieFieldsSchema, toMovieFields, toMovieToSave } from '../../movie-form';
import { form, FormField, FormRoot } from '@angular/forms/signals';
import { toSubmissionErrors, ValidationErrors } from '../../../shared/form-errors';

function toDeleteErrorMessage(error: unknown): string {
  const problem =
    error instanceof HttpErrorResponse ? (error.error as ValidationErrors | null) : null;
  return problem?.errors?.[0]?.message ?? 'Could not delete, try again';
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

  deleting = signal(false);
  deleteError = signal<string | null>(null);

  async deleteMovie() {
    if (!confirm('Delete this movie? This cannot be undone.')) {
      return;
    }
    this.deleting.set(true);
    this.deleteError.set(null);
    try {
      await this.movieApi.delete(this.movieId);
    } catch (e) {
      this.deleteError.set(toDeleteErrorMessage(e));
      this.deleting.set(false);
      return;
    }
    await this.router.navigateByUrl('/movies');
  }

  private movieFieldsModel = linkedSignal(() =>
    toMovieFields(this.movie.hasValue() ? this.movie.value() : undefined),
  );

  movieFieldsForm = form(this.movieFieldsModel, movieFieldsSchema, {
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
  });
}

export const EditMoviePageRoute: Route = {
  path: 'edit/movie/:id',
  component: EditMoviePage,
  title: 'Edit movie',
};
