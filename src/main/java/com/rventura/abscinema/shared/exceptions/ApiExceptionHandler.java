package com.rventura.abscinema.shared.exceptions;

import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
        MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<ValidationError> errors = new ArrayList<>();
        ex.getBindingResult().getFieldErrors()
            .forEach(e -> errors.add(new ValidationError(rootField(e.getField()), e.getDefaultMessage())));
        ex.getBindingResult().getGlobalErrors()
            .forEach(e -> errors.add(new ValidationError(null, e.getDefaultMessage())));

        ProblemDetail body = ex.getBody();
        body.setDetail("Some fields are invalid");
        body.setProperty("errors", errors);
        return handleExceptionInternal(ex, body, headers, status, request);
    }

    @ExceptionHandler(InvalidInputOnSaveException.class)
    public ProblemDetail handleInvalidInputOnSave(InvalidInputOnSaveException e) {
        String message = "This could not be saved, check the fields and try again";
        ProblemDetail body = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, message);
        body.setProperty("errors", List.of(new ValidationError(null, message)));
        return body;
    }

    // Bean Validation reports list items as "genres[0]", but forms have one input per property.
    private static String rootField(String path) {
        int end = path.length();
        for (char separator : new char[] {'[', '.'}) {
            int index = path.indexOf(separator);
            if (index >= 0 && index < end) {
                end = index;
            }
        }
        return path.substring(0, end);
    }
}
