import { Component, inject, linkedSignal } from '@angular/core';
import { ActivatedRoute, Route, Router, RouterLink } from '@angular/router';
import { MovieApi } from '../../movie';
import { movieFieldsSchema, toMovieFields, toMovieToSave } from '../../movie-form';
import { form, FormField, FormRoot } from '@angular/forms/signals';
import { toSubmissionErrors } from '../../../shared/form-errors';

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
