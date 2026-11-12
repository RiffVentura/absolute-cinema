import { ApplicationRef } from '@angular/core';
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

  it('loads a single movie by id', async () => {
    const movie = TestBed.runInInjectionContext(() => api.movie(() => 1));
    TestBed.tick();

    http.expectOne('/internal/movies/').flush([]);
    http.expectOne((r) => r.method === 'GET' && r.url === '/internal/movies/1').flush(savedMovie);
    await TestBed.inject(ApplicationRef).whenStable();

    expect(movie.value()).toEqual(savedMovie);
  });

  it('puts the changed movie and resolves with the saved one', async () => {
    const result = api.update(1, newMovie);

    const req = http.expectOne((r) => r.method === 'PUT' && r.url === '/internal/movies/1');
    expect(req.request.body).toEqual(newMovie);
    req.flush(savedMovie);

    await expect(result).resolves.toEqual(savedMovie);
  });
});
