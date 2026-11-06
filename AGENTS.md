# AGENTS.md

This file provides guidance to AI coding agents (Claude Code, Codex, etc.) when working with code in this repository.

## Overview

Absolute Cinema is a cinema management app. It has two parts:
- **Backend**: Spring Boot 4 (Java 27, Maven) in `src/`, backed by PostgreSQL. Uses Spring Data JDBC / `NamedParameterJdbcTemplate` (no JPA), Flyway, Spring Security, and Thymeleaf.
- **Admin frontend**: Angular 22 SPA in `frontend/`. It uses standalone components, signals, and signal forms (`@angular/forms/signals`), and its tests run on Vitest.

## Project vision

Absolute Cinema is a back office for running a cinema, plus a public site for moviegoers.
- **Back office (`/admin/**`)**: CRUD for movies, halls/screens, and schedules (session time slots). More features may come later.
- **Public site (`/`)**: needs no login. Visitors can browse movies, see a movie's details, and view its schedule.
- **Public REST API (`/api/**`)**: reserved for a future user-facing API. Don't put admin endpoints here; those go under `/internal/**`.

## Architecture decisions (settled; don't revisit without asking)

- **Angular is used only for the admin back office.** Public, end-user pages are rendered server-side with Thymeleaf. Don't add Angular routes or a JS framework to public pages.
- **No ORM.** Don't use JPA, Hibernate, or Spring Data repository derivation. All persistence is handwritten SQL through JDBC (`NamedParameterJdbcTemplate`) inside `adapters/out` classes.
- **The backend is hexagonal and grouped by feature.** Each feature package (`user/`, `movies/`, `halls/`, `schedule/`, ...) has its own `domain/`, `application/` (services and ports), `adapters/in/`, and `adapters/out/`. Don't create global layer packages such as `controllers/` or `repositories/`.

## Commands

Backend (repo root):
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev   # run app on :8080; spring-boot-docker-compose starts Postgres from compose.yaml
./mvnw test                                             # all tests; needs Postgres running (see "Tests" below)
./mvnw test -Dtest=AbsCinemaApplicationTests#contextLoads   # single test
./mvnw package
```

Frontend (`frontend/`). The package manager is **bun**: use `bun install` / `bun add`, never npm or yarn, and commit only `bun.lock`. Run the Angular CLI as `bunx ng ...` or `bun run ...`:
```bash
ng serve        # dev server on :4200, proxies /api, /internal, /uploads, /login, /logout to :8080 (proxy.conf.json)
ng build        # writes into ../src/main/resources/static/admin (gitignored), served by Spring at /admin/
ng test         # Vitest via @angular/build:unit-test
ng test --include src/app/movies/pages/add-movie/add-movie.spec.ts   # single spec
```
Formatting: Prettier (`frontend/.prettierrc`, 100 cols, single quotes).

Frontend rules:
- **Styling is plain CSS.** Use component-scoped `.css` files and shared rules in `src/styles.css`. Don't add a UI or component library or a CSS framework (Material, Tailwind, Bootstrap, ...). A design system will come later, so keep styles simple and put repeated values (colors, spacing) in CSS custom properties, which makes them easy to migrate.
- **The frontend's TypeScript types mirror the backend DTOs exactly** (field names, optionality, ISO-8601 date strings). When a DTO changes, change the TS type in the same commit.
- **Tests** (Vitest + jsdom, no backend):
  - API classes such as `MovieApi`: use `provideHttpClientTesting()` + `HttpTestingController` to check the URL, method, body, and error handling.
  - Page components: use a fake API class provided in place of the real one (the same idea as the backend port fakes), plus `provideRouter([])` when the template uses router directives.
  - Every `.spec.ts` must contain at least one test. Vitest fails on files with no tests.

### Checking UI work in a real browser (Playwright)
- The devcontainer image includes headless Chromium in `/ms-playwright` (`PLAYWRIGHT_BROWSERS_PATH` is set). The library is `@playwright/test`, pinned exactly in `frontend/package.json`.
- **Keep the versions in sync.** When you bump `@playwright/test`, change `PLAYWRIGHT_VERSION` in `.devcontainer/Dockerfile` to the same version. Otherwise the library looks for a browser build that isn't installed.
- After changing the UI, check it for real. Run `bunx ng build`, then start the backend (`./mvnw spring-boot:run`, dev profile, admin/admin). Drive `http://localhost:8080/admin/` with a throwaway Playwright script and look at screenshots: log in through the `/login` form, then exercise the flow.
  - Put the scripts in your scratchpad, not the repo. Run them as CommonJS with `NODE_PATH=/workspaces/absolute-cinema/frontend/node_modules node script.cjs`.
- There's no browser end-to-end test suite yet. When one is added, it lives in `frontend/e2e/`.

## Architecture

### How the backend serves the frontend
- The Angular build output goes into `src/main/resources/static/admin` with `baseHref: /admin/`. `config/AdminSpaConfig` serves `/admin/**` from there and falls back to `index.html` so SPA deep links work. Rebuild the frontend before running the backend standalone if you need the admin UI.
- `/` is a public Thymeleaf page (`showcase/ShowcaseController`, which renders `templates/movies.html`).

### Security (`security/SecurityConfig`)
- `/admin/**` and `/internal/**` require the `ADMIN` authority. Everything else is `permitAll`.
- It uses form login (which redirects to `/admin/` on success) plus HTTP Basic. CSRF uses `csrf.spa()`, so the Angular app must send the XSRF token on mutating requests.
- Unauthenticated `/api/**` requests get a 401 instead of a redirect.
- URL convention: `/internal/...` is for admin-only JSON endpoints used by the SPA (e.g. `/internal/movies`), and `/api/...` is for public JSON.
- Users and roles live in the `users`, `roles` and `user_roles` tables, and `DBUserDetailsService` loads them. On startup, `InitDefaultAdminAppRunner` creates an admin from `app.default.admin` / `app.default.admin-password` (env `DEFAULT_ADMIN`, `DEFAULT_ADMIN_PASSWORD`) if both are set and the user doesn't already exist. The `dev` profile sets them to `admin`/`admin`.

### Package layout
- `user/` follows hexagonal (ports and adapters) structure, and new features with persistence should follow it:
  - `domain/`: plain records (`User`, `UserToCreate`)
  - `application/`: services plus port interfaces (`UserRepositoryPort`)
  - `adapters/in/`: entry points (runners, controllers)
  - `adapters/out/`: implementations (`DBUserRepository`, which runs handwritten SQL through `NamedParameterJdbcTemplate`)
- `movies/` is still flat and stubbed: the controller returns hardcoded data. `MovieToSave` is the validated input record.
- `shared/exceptions/`: `AppException` and `InvalidInputOnSaveException`. Repositories translate DB errors such as `DuplicateKeyException` into these.
- Naming pattern: a `Foo` record is the persisted entity, and a `FooToCreate`/`FooToSave` record is the input DTO.

### Database
- Flyway migrations live in `src/main/resources/db/migration`, named `V<yyyyMMddHHmmss>__description.sql`.
- Schema so far: `movies` (`genres` is `TEXT[]`), `halls`, `movie_sessions` (composite PK of movie, hall and start time), and the user/role tables.
- `docker/postgres-init/` is mounted as `/docker-entrypoint-initdb.d` in both compose files. It creates the `abscinema_test` database, but only when Postgres initializes a fresh data directory.
- `application.yaml` reads the datasource from `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER` and `DB_PASSWORD`. `application-dev.yaml` points it at the compose Postgres (`abscinema` / `myuser` / `secret`).

### Tests
Only SQL and full-stack tests touch Postgres. Everything else uses in-memory fakes of the ports. Never use H2 or another in-memory SQL database: the migrations are Postgres-specific.

| Layer | What to test | Test type | Database |
|---|---|---|---|
| `adapters/out` (repositories) | The handwritten SQL, mapping, and DB error translation into `shared/exceptions` | `@JdbcTest` + `@AutoConfigureTestDatabase(replace = NONE)` + `@Import` of the repository | Real Postgres (`abscinema_test`) |
| `application` (services) | Business rules | Plain JUnit, no Spring. Construct the service with an in-memory port fake | None |
| `adapters/in` (controllers) | HTTP: routing, JSON, validation, security (401/403, CSRF) | `@WebMvcTest`, with the service built on in-memory port fakes | None |
| True end-to-end | A whole feature across all layers | `@SpringBootTest` (full context, real HTTP or `MockMvc`) | Real Postgres (`abscinema_test`) |

- **In-memory port fakes** live in test sources, next to the port's package: for example, `src/test/java/.../movies/application/InMemoryMovieRepository implements MovieRepositoryPort`, backed by a `Map`. Each port has one shared fake, not one per test. Fakes must behave like the real adapter where it matters, such as assigning IDs or throwing `InvalidInputOnSaveException` on duplicates.
- Prefer fakes to Mockito mocks for ports. Use mocks only for interactions a fake can't express.
- **Postgres tests** use the `abscinema_test` database (`src/test/resources/application-test.yaml`), so they never touch dev data. They must use `@ActiveProfiles("test")`.
  - In the devcontainer, that is the `postgres` sidecar, which is always running.
  - On the host, run `docker compose up -d` first. Spring's docker-compose support is skipped in tests.
  - The test database persists between runs, and Flyway keeps its schema current. Tests must leave no data behind: use `@Transactional` rollback, or truncate the tables they touch.

### Frontend structure
- Features live in folders (`movies/`, `screens/`, `schedule/`). Each page component exports its own `Route` object (e.g. `AddMoviePageRoute`), and `app.routes.ts` spreads or collects them.
- API access goes through injectable classes such as `MovieApi` in `movies/movie.ts`.

## Conventions
- Commit messages are prefixed with a ticket ID: `ABS-NNN: <summary>`.
- **Comments, in source code and config files alike:** write one only to explain *why*, and only when the reason isn't obvious from the code itself, such as a workaround, a hidden constraint, or a surprising choice. Never comment *what* the code does, never restate a name or key, and never leave commented-out code or section-banner comments.
