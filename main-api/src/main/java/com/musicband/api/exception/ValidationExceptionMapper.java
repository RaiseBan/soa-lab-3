package com.musicband.api.exception;

import com.musicband.api.model.Error;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.stream.Collectors;

/**
 * Exception mapper for Bean Validation errors in Main API
 */
@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        String details = exception.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));

        Error error = new Error(
                422,
                "Business validation failed",
                details.isEmpty() ? "Validation constraints violated" : details
        );

        return Response.status(422).entity(error).build();
    }
}