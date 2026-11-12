import { resource } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, provideRouter, Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { Movie, MovieApi, MovieToSave } from '../../movie';
import { EditMoviePageRoute } from './edit-movie';

const alien: Movie = {
  id: 7,
  title: 'Alien',
  director: 'Ridley Scott',
  mainCast: 'Sigourney Weaver',
  genres: ['Horror', 'Sci-Fi'],
  runtimeMinutes: 117,
  releaseDate: '1979-05-25',
  synopsis: null,
  posterUrl: null,
  createdAt: '2026-11-06T16:00:00Z',
};

class FakeMovieApi {
  movies = new Map<number, Movie>([[alien.id, alien]]);
  updated: { id: number; movie: MovieToSave }[] = [];
  failWith: HttpErrorResponse | null = null;

  movie(id: () => number) {
    return resource({
      params: id,
      loader: async ({ params }) => {
        const movie = this.movies.get(params);
        if (!movie) {
          throw new HttpErrorResponse({ status: 404 });
        }
        return movie;
      },
    });
  }

  async update(id: number, movie: MovieToSave): Promise<Movie> {
    if (this.failWith) {
      throw this.failWith;
    }
    this.updated.push({ id, movie });
    return { ...this.movies.get(id)!, ...movie } as Movie;
  }
}

async function setUp(id = alien.id) {
  const api = new FakeMovieApi();
  TestBed.configureTestingModule({
    providers: [
      provideRouter([]),
      { provide: MovieApi, useValue: api },
      {
        provide: ActivatedRoute,
        useValue: { snapshot: { paramMap: convertToParamMap({ id: String(id) }) } },
      },
    ],
  });
  const fixture = TestBed.createComponent(EditMoviePageRoute.component!);
  await fixture.whenStable();
  fixture.detectChanges();
  const el: HTMLElement = fixture.nativeElement;
  const input = (id: string) => el.querySelector<HTMLInputElement>(`#${id}`)!;
  const type = (id: string, value: string) => {
    input(id).value = value;
    input(id).dispatchEvent(new Event('input'));
  };
  const submit = async () => {
    el.querySelector('form')!.dispatchEvent(new Event('submit'));
    await fixture.whenStable();
    fixture.detectChanges();
  };
  return { api, fixture, el, input, type, submit };
}

describe('EditMoviePage', () => {
  it('fills the form with the loaded movie', async () => {
    const { input } = await setUp();

    expect(input('title').value).toBe('Alien');
    expect(input('director').value).toBe('Ridley Scott');
    expect(input('mainCast').value).toBe('Sigourney Weaver');
    expect(input('genres').value).toBe('Horror, Sci-Fi');
    expect(input('runtimeMinutes').value).toBe('117');
    expect(input('releaseDate').value).toBe('1979-05-25');
  });

  it('shows an error when the movie cannot be loaded', async () => {
    const { el } = await setUp(999);

    expect(el.querySelector('form')).toBeNull();
    expect(el.querySelector('.form-error')?.textContent).toContain('Could not load');
  });

  it('keeps submit disabled while a required field is empty', async () => {
    const { el, fixture, type } = await setUp();

    type('title', '');
    fixture.detectChanges();

    expect(el.querySelector<HTMLButtonElement>('button[type=submit]')!.disabled).toBe(true);
  });

  it('sends the changed movie and goes back to the list', async () => {
    const { api, type, submit } = await setUp();
    const navigate = vi.spyOn(TestBed.inject(Router), 'navigateByUrl').mockResolvedValue(true);

    type('title', 'Aliens');
    type('genres', ' Action, , Sci-Fi ');
    await submit();

    expect(api.updated).toEqual([
      {
        id: 7,
        movie: {
          title: 'Aliens',
          director: 'Ridley Scott',
          mainCast: 'Sigourney Weaver',
          genres: ['Action', 'Sci-Fi'],
          runtimeMinutes: 117,
          releaseDate: '1979-05-25',
          synopsis: null,
        },
      },
    ]);
    expect(navigate).toHaveBeenCalledWith('/movies');
  });

  it('shows an error when the server rejects the changes', async () => {
    const { api, el, submit } = await setUp();
    api.failWith = new HttpErrorResponse({ status: 400 });

    await submit();

    expect(el.querySelector('.form-error')?.textContent).toContain('rejected');
  });
});
