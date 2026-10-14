package com.rventura.abscinema.user.application;

import java.util.List;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.rventura.abscinema.security.Role;
import com.rventura.abscinema.user.domain.User;
import com.rventura.abscinema.user.domain.UserToCreate;

@Service
public class DefaultAdminUserInitialzer {
    private final UserRepositoryPort userRepository;
    private final PasswordEncoder passwordEncoder;

    public DefaultAdminUserInitialzer(
        UserRepositoryPort userRepository,
        PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }


    public void init(String username, String password) {
        if (username.length() == 0 || password.length() == 0) {
            return;
        }

        Optional<User> foundUser = this.userRepository.findByUsername(username);

        if (foundUser.isPresent()) {
            return;
        }

        String passwordHash = passwordEncoder.encode(password);

        this.userRepository.create(new UserToCreate(username, passwordHash, username, username),
            List.of(Role.USER, Role.ADMIN));


    }
}
