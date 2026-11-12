package com.rventura.abscinema.movies.adapters.in;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.matchesRegex;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import com.rventura.abscinema.movies.application.InMemoryMovieRepository;
import com.rventura.abscinema.movies.application.MovieService;
import com.rventura.abscinema.security.SecurityConfig;

@WebMvcTest(MoviesController.class)
@Import({SecurityConfig.class, MoviesControllerTest.Config.class})
class MoviesControllerTest {

    @TestConfiguration
    static class Config {
        @Bean
        MovieService movieService() {
            return new MovieService(new InMemoryMovieRepository());
        }
    }

    private static final String VALID_MOVIE = """
        {
          "title": "The Matrix",
          "director": "The Wachowskis",
          "mainCast": "Keanu Reeves",
          "genres": ["Sci-Fi", "Action"],
          "runtimeMinutes": 136,
          "releaseDate": "1999-03-31",
          "synopsis": "A hacker learns the truth."
        }
        """;

    @Autowired
    private MockMvc mvc;

    @Test
    @WithMockUser(authorities = "ADMIN")
    void createReturns201WithSavedMovie() throws Exception {
        mvc.perform(post("/internal/movies/").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_MOVIE))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", matchesRegex("/internal/movies/\\d+")))
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.title").value("The Matrix"))
            .andExpect(jsonPath("$.genres[1]").value("Action"))
            .andExpect(jsonPath("$.releaseDate").value("1999-03-31"))
            .andExpect(jsonPath("$.posterUrl").doesNotExist());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void createdMovieAppearsInList() throws Exception {
        mvc.perform(post("/internal/movies/").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_MOVIE))
            .andExpect(status().isCreated());

        mvc.perform(get("/internal/movies/"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].title").value("The Matrix"));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void createRejectsMissingRequiredFields() throws Exception {
        mvc.perform(post("/internal/movies/").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "title": "  ", "runtimeMinutes": 136 }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors[*].field").value(containsInAnyOrder("title", "director")))
            .andExpect(jsonPath("$.errors[?(@.field == 'title')].message").value("Movie needs a title"));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void createRejectsNonPositiveRuntime() throws Exception {
        mvc.perform(post("/internal/movies/").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "title": "Alien", "director": "Ridley Scott", "runtimeMinutes": 0 }
                    """))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void createRejectsBlankGenre() throws Exception {
        mvc.perform(post("/internal/movies/").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "title": "Alien", "director": "Ridley Scott", "genres": [""] }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors[0].field").value("genres"))
            .andExpect(jsonPath("$.errors[0].message").value("Genres can't be empty"));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void createWithoutCsrfTokenIsForbidden() throws Exception {
        mvc.perform(post("/internal/movies/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_MOVIE))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "USER")
    void createAsNonAdminIsForbidden() throws Exception {
        mvc.perform(post("/internal/movies/").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_MOVIE))
            .andExpect(status().isForbidden());
    }

    @Test
    void createAnonymouslyIsRejected() throws Exception {
        mvc.perform(post("/internal/movies/").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_MOVIE))
            .andExpect(status().is4xxClientError());
    }
}
