package com.rventura.abscinema.shared.exceptions;

public record ValidationError(String field, String message) {
}
