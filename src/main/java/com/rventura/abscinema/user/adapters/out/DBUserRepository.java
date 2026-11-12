package com.rventura.abscinema.user.adapters.out;


import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import com.rventura.abscinema.shared.exceptions.InvalidInputOnSaveException;
import com.rventura.abscinema.security.Role;
import com.rventura.abscinema.user.application.UserRepositoryPort;
import com.rventura.abscinema.user.domain.User;
import com.rventura.abscinema.user.domain.UserToCreate;

@Repository
public class DBUserRepository implements UserRepositoryPort {

    private final NamedParameterJdbcTemplate jdbc;

    private final RowMapper<User> rowMapper = ((rs, rowNum) -> new User(
        rs.getLong("id"),
        rs.getString("email"),
        rs.getString("first_name"),
        rs.getString("last_name"),
        rs.getBoolean("active")));

    public DBUserRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.jdbc = namedParameterJdbcTemplate;
    }

    public User create(UserToCreate user, List<Role> roles) throws InvalidInputOnSaveException {
        String sql = """
            INSERT INTO users (username, password, first_name, last_name)
            VALUES (:username, :password, :firstName, :lastName)
            RETURNING id, email, active
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("username", user.username())
            .addValue("password", user.passwordHash())
            .addValue("firstName", user.firstName())
            .addValue("lastName", user.lastName());


        User createdUser;
        try {
            createdUser = jdbc.queryForObject(sql, params, (rs, rowNum) -> {
                return new User(
                    rs.getLong("id"),
                    rs.getString("email"),
                    user.firstName(),
                    user.lastName(),
                    rs.getBoolean("active"));
            });
        } catch (DataIntegrityViolationException e) {
            throw new InvalidInputOnSaveException(e);
        }

        addRolesToUser(createdUser.id(), roles);

        return createdUser;
    }

    private void addRolesToUser(Long userId, List<Role> roles) {
        String sql = """
            INSERT INTO user_roles(user_id, role_id)
            SELECT :userId as user_id, roles.id
            FROM roles
            WHERE roles.name IN (:roleNames)
            """;

        List<String> rolesNames = roles.stream().map(Role::name).collect(Collectors.toList());

        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("userId", userId)
            .addValue("roleNames", rolesNames);

        jdbc.update(sql, params);
    }

    public Optional<User> findByUsername(String username) {
        String sql = """
            SELECT id, email, first_name, last_name, active
            FROM users
            WHERE username = :userName
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("userName", username);

        try {
            User user = jdbc.queryForObject(sql, params, rowMapper);
            return Optional.of(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
