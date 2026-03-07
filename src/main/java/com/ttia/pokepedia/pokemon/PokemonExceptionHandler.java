package com.ttia.pokepedia.pokemon;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class PokemonExceptionHandler {

    @ExceptionHandler(PokemonNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleNotFound(PokemonNotFoundException e) {
        return Map.of("error", e.getMessage());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidation(ConstraintViolationException e) {
        String invalidValue = e.getConstraintViolations().iterator().next().getInvalidValue().toString();
        return Map.of("error", "Invalid Pokemon name: \"" + invalidValue + "\"");
    }
}
