package com.musicband.api.exception;

import com.musicband.api.model.Error;
import jakarta.persistence.PersistenceException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.sql.SQLException;

@Provider
public class PersistenceExceptionMapper implements ExceptionMapper<PersistenceException> {

    @Override
    public Response toResponse(PersistenceException exception) {
        Throwable cause = exception.getCause();
        SQLException sqlException = findSQLException(cause);

        if (sqlException != null) {
            String sqlState = sqlException.getSQLState();
            String message = sqlException.getMessage();

            if ("22001".equals(sqlState) ||
                    (message != null && message.toLowerCase().contains("value too long"))) {
                Error error = new Error(
                        422,
                        "Validation failed",
                        "One or more text fields exceed the maximum length of 255 characters"
                );
                return Response.status(422).entity(error).build();
            }

            if ("23505".equals(sqlState) ||
                    (message != null && message.toLowerCase().contains("duplicate"))) {
                Error error = new Error(
                        422,
                        "Validation failed",
                        "A record with this data already exists"
                );
                return Response.status(422).entity(error).build();
            }
        }

        String errorMessage = exception.getMessage();
        if (errorMessage != null) {
            String lowerMsg = errorMessage.toLowerCase();

            if (lowerMsg.contains("too long") ||
                    lowerMsg.contains("data truncation") ||
                    lowerMsg.contains("string data, right truncation") ||
                    lowerMsg.contains("value too long for type character varying")) {
                Error error = new Error(
                        422,
                        "Validation failed",
                        "One or more text fields exceed the maximum length of 255 characters"
                );
                return Response.status(422).entity(error).build();
            }

            if (lowerMsg.contains("duplicate") || lowerMsg.contains("unique")) {
                Error error = new Error(
                        422,
                        "Validation failed",
                        "A record with this data already exists"
                );
                return Response.status(422).entity(error).build();
            }
        }

        Error error = new Error(
                500,
                "Database error",
                "An error occurred while processing your request"
        );

        exception.printStackTrace();
        return Response.status(500).entity(error).build();
    }

    private SQLException findSQLException(Throwable throwable) {
        if (throwable == null) {
            return null;
        }

        if (throwable instanceof SQLException) {
            return (SQLException) throwable;
        }

        return findSQLException(throwable.getCause());
    }
}
