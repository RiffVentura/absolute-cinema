import { Component, inject, signal } from '@angular/core';
import { Route, Router, RouterLink } from '@angular/router';
import { MovieApi } from '../../movie';
import { movieFieldsSchema, toMovieFields, toMovieToSave } from '../../movie-form';
import { form, FormField, FormRoot } from '@angular/forms/signals';
import { toSubmissionErrors } from '../../../shared/form-errors';

@Component({
  selector: 'app-movie-add',
  templateUrl: './add-movie.html',
  styleUrl: './add-movie.css',
  imports: [RouterLink, FormField, FormRoot],
})
class AddMoviePage {
  private movieApi = inject(MovieApi);
  private router = inject(Router);

  private movieFieldsModel = signal(toMovieFields());

  movieFieldsForm = form(this.movieFieldsModel, movieFieldsSchema, {
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
  });
}

export const AddMoviePageRoute: Route = {
  path: 'new/movie',
  component: AddMoviePage,
  title: 'Add a new movie',
};
