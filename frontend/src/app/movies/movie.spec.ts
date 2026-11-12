import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Movie, MovieApi, MovieToSave } from './movie';

const newMovie: MovieToSave = {
  title: 'Alien',
  director: 'Ridley Scott',
  genres: ['Horror'],
  runtimeMinutes: 117,
  releaseDate: '1979-05-25',
};

const savedMovie: Movie = {
  id: 1,
  title: 'Alien',
  director: 'Ridley Scott',
  mainCast: null,
  genres: ['Horror'],
  runtimeMinutes: 117,
  releaseDate: '1979-05-25',
  synopsis: null,
  posterUrl: null,
  createdAt: '2026-11-06T16:00:00Z',
};

describe('MovieApi', () => {
  let api: MovieApi;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    api = TestBed.inject(MovieApi);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify({ ignoreCancelled: true }));

  it('posts the new movie and resolves with the saved one', async () => {
    const result = api.create(newMovie);

    const req = http.expectOne((r) => r.method === 'POST' && r.url === '/internal/movies/');
    expect(req.request.body).toEqual(newMovie);
    req.flush(savedMovie, { status: 201, statusText: 'Created' });

    await expect(result).resolves.toEqual(savedMovie);
  });

  it('rejects when the server refuses the movie', async () => {
    const result = api.create(newMovie);

    http
      .expectOne((r) => r.method === 'POST')
      .flush({ title: 'Bad Request' }, { status: 400, statusText: 'Bad Request' });

    await expect(result).rejects.toMatchObject({ status: 400 });
  });
});
