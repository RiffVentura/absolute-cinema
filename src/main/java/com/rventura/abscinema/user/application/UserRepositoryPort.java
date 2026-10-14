package com.rventura.abscinema.user.application;

import java.util.List;
import java.util.Optional;
import com.rventura.abscinema.shared.exceptions.InvalidInputOnSaveException;
import com.rventura.abscinema.security.Role;
import com.rventura.abscinema.user.domain.User;
import com.rventura.abscinema.user.domain.UserToCreate;

public interface UserRepositoryPort {
    User create(UserToCreate user, List<Role> roles) throws InvalidInputOnSaveException;

    Optional<User> findByUsername(String username);
}
