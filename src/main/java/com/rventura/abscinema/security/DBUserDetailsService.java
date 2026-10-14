package com.rventura.abscinema.security;

import java.util.Collection;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class DBUserDetailsService implements UserDetailsService {

    private final NamedParameterJdbcTemplate jdbc;

    public DBUserDetailsService(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.jdbc = namedParameterJdbcTemplate;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        String sql = """
            SELECT username, password FROM users WHERE username = :username
            """;
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("username", username);

        String getRolesSQL = """
            SELECT roles.name
            FROM users
            JOIN user_roles ON users.id =  user_roles.user_id
            JOIN roles ON user_roles.role_id = roles.id
            WHERE users.username = :username
            """;

        Collection<GrantedAuthority> authorities = jdbc.query(getRolesSQL, params,
            (rs, row) -> new SimpleGrantedAuthority(rs.getString("name")));

        if (authorities.isEmpty()) {
            throw new UsernameNotFoundException("No authorities found for user: " + username);
        }

        try {
            User user = jdbc.queryForObject(sql, params,
                (rs, row) -> new User(
                    rs.getString("username"),
                    rs.getString("password"),
                    authorities));
            return user;
        } catch (EmptyResultDataAccessException e) {
            throw new UsernameNotFoundException("User not found: " + username);
        }
    }
}

