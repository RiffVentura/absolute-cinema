package com.rventura.abscinema.user.domain;

public record User(
    Long id,
    String email,
    String firstName,
    String lastName,
    Boolean isActive) {
}
