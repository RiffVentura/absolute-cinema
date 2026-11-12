package com.rventura.abscinema.movies.adapters.out;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import com.rventura.abscinema.movies.application.MovieRepositoryPort;
import com.rventura.abscinema.movies.domain.Movie;
import com.rventura.abscinema.movies.domain.MovieToSave;
import com.rventura.abscinema.shared.exceptions.InvalidInputOnSaveException;

@Repository
public class DBMovieRepository implements MovieRepositoryPort {
    private static final String COLUMNS = """
        id, title, director, main_cast, genres, runtime_minutes, release_date,
        synopsis, poster_url, created_at
        """;

    private final NamedParameterJdbcTemplate jdbc;

    private final RowMapper<Movie> rowMapper = (rs, rowNum) -> new Movie(
        rs.getLong("id"),
        rs.getString("title"),
        rs.getString("director"),
        rs.getString("main_cast"),
        toList(rs.getArray("genres")),
        rs.getObject("runtime_minutes", Integer.class),
        rs.getObject("release_date", LocalDate.class),
        rs.getString("synopsis"),
        rs.getString("poster_url"),
        rs.getObject("created_at", OffsetDateTime.class));

    public DBMovieRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Movie create(MovieToSave movie) throws InvalidInputOnSaveException {
        String sql = """
            INSERT INTO movies (title, director, main_cast, genres, runtime_minutes, release_date, synopsis)
            VALUES (:title, :director, :mainCast, :genres, :runtimeMinutes, :releaseDate, :synopsis)
            RETURNING
            """ + COLUMNS;

        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("title", movie.title())
            .addValue("director", movie.director())
            .addValue("mainCast", movie.mainCast())
            .addValue("genres", movie.genres().toArray(String[]::new))
            .addValue("runtimeMinutes", movie.runtimeMinutes())
            .addValue("releaseDate", movie.releaseDate())
            .addValue("synopsis", movie.synopsis());

        try {
            return jdbc.queryForObject(sql, params, rowMapper);
        } catch (DataIntegrityViolationException e) {
            throw new InvalidInputOnSaveException(e);
        }
    }

    @Override
    public List<Movie> findAll() {
        String sql = "SELECT " + COLUMNS + " FROM movies ORDER BY created_at DESC, id DESC";
        return jdbc.query(sql, rowMapper);
    }

    private static List<String> toList(Array array) throws SQLException {
        if (array == null) {
            return List.of();
        }
        return List.of((String[]) array.getArray());
    }
}
