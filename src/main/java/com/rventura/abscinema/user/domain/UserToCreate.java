package com.rventura.abscinema.user.domain;

/**
 * UserToCreate only for initial save in persistence StorageResolverException (no id, no timestamps)
 */
public record UserToCreate(
    String username, String passwordHash, String firstName, String lastName) {
}
