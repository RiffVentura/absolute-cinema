import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { Movie, MovieApi, MovieToSave } from '../../movie';
import { AddMoviePageRoute } from './add-movie';

class FakeMovieApi {
  saved: MovieToSave[] = [];
  failWith: HttpErrorResponse | null = null;

  async create(movie: MovieToSave): Promise<Movie> {
    if (this.failWith) {
      throw this.failWith;
    }
    this.saved.push(movie);
    return {
      id: this.saved.length,
      mainCast: null,
      genres: [],
      runtimeMinutes: null,
      releaseDate: null,
      synopsis: null,
      posterUrl: null,
      createdAt: '2026-11-06T16:00:00Z',
      ...movie,
    };
  }
}

function setUp() {
  const api = new FakeMovieApi();
  TestBed.configureTestingModule({
    providers: [provideRouter([]), { provide: MovieApi, useValue: api }],
  });
  const fixture = TestBed.createComponent(AddMoviePageRoute.component!);
  fixture.detectChanges();
  const el: HTMLElement = fixture.nativeElement;
  const type = (id: string, value: string) => {
    const input = el.querySelector<HTMLInputElement>(`#${id}`)!;
    input.value = value;
    input.dispatchEvent(new Event('input'));
  };
  const submit = async () => {
    el.querySelector('form')!.dispatchEvent(new Event('submit'));
    await fixture.whenStable();
    fixture.detectChanges();
  };
  const submitButton = () => el.querySelector<HTMLButtonElement>('button[type=submit]')!;
  return { api, fixture, el, type, submit, submitButton };
}

describe('AddMoviePage', () => {
  it('keeps submit disabled until title and director are filled', () => {
    const { fixture, type, submitButton } = setUp();
    expect(submitButton().disabled).toBe(true);

    type('title', 'Alien');
    fixture.detectChanges();
    expect(submitButton().disabled).toBe(true);

    type('director', 'Ridley Scott');
    fixture.detectChanges();
    expect(submitButton().disabled).toBe(false);
  });

  it('sends the cleaned up movie and goes back to the list', async () => {
    const { api, type, submit } = setUp();
    const navigate = vi.spyOn(TestBed.inject(Router), 'navigateByUrl').mockResolvedValue(true);

    type('title', 'Alien');
    type('director', 'Ridley Scott');
    type('genres', ' Horror, , Sci-Fi ');
    type('runtimeMinutes', '117');
    type('releaseDate', '1979-05-25');
    await submit();

    expect(api.saved).toEqual([
      {
        title: 'Alien',
        director: 'Ridley Scott',
        mainCast: null,
        genres: ['Horror', 'Sci-Fi'],
        runtimeMinutes: 117,
        releaseDate: '1979-05-25',
        synopsis: null,
      },
    ]);
    expect(navigate).toHaveBeenCalledWith('/movies');
  });

  it('shows an error when the server rejects the movie', async () => {
    const { api, el, type, submit } = setUp();
    api.failWith = new HttpErrorResponse({ status: 400 });

    type('title', 'Alien');
    type('director', 'Ridley Scott');
    await submit();

    expect(el.querySelector('.form-error')?.textContent).toContain('rejected');
  });

  it('shows server field errors under the matching field until it is edited', async () => {
    const { api, el, fixture, type, submit } = setUp();
    api.failWith = new HttpErrorResponse({
      status: 400,
      error: {
        type: 'about:blank',
        title: 'Bad Request',
        status: 400,
        detail: 'Some fields are invalid',
        errors: [
          { field: 'genres', message: 'Each genre is limited to 50 characters' },
          { field: null, message: 'Something else went wrong' },
        ],
      },
    });

    type('title', 'Alien');
    type('director', 'Ridley Scott');
    type('genres', 'Horror');
    await submit();

    const genresRow = el.querySelector('#genres')!.closest('.form-row')!;
    expect(genresRow.querySelector('.validation-error')?.textContent).toContain('50 characters');
    expect(el.querySelector('.form-error')?.textContent).toContain('Something else went wrong');

    type('genres', 'Sci-Fi');
    fixture.detectChanges();
    expect(genresRow.querySelector('.validation-error')).toBeNull();
  });
});
