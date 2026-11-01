import { Component, inject, signal } from "@angular/core";
import { Route, RouterLink } from "@angular/router";
import { MovieApi } from "../../movie";
import { form, FormField, FormRoot, required } from "@angular/forms/signals";

interface MovieFields {
  title: string;
  description: string;
  releaseDate: Date;
}

@Component({
  selector: 'app-movie-add',
  templateUrl: './add-movie.html',
  styleUrl: './add-movie.css',
  imports: [RouterLink, FormField, FormRoot],
})
class AddMoviePage {
  private movieApi = inject(MovieApi);

  private movieFieldsModel = signal<MovieFields>({
    title: '',
    description: '',
    releaseDate: new Date(),
  });

  movieFieldsForm = form(
    this.movieFieldsModel,
    (schemaPath) => {
      required(schemaPath.title, { message: 'Movie needs a title' });
      required(schemaPath.releaseDate, { message: 'Movie needs a release date' });
    },
    {
      submission: {
        action: async (data) => {
          this.movieApi.create(data().value());
          return;
        }
      }
    },
  );
}

export const AddMoviePageRoute: Route = {
  path: 'new/movie',
  component: AddMoviePage,
  title: 'Add a new movie'
}
