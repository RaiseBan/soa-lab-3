package com.musicband.api.exception;

import com.musicband.api.model.Error;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * General exception mapper for Main API
 * Handles unexpected errors and returns proper XML error response
 */
@Provider
public class GeneralExceptionMapper implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception exception) {
        // If it's already a WebApplicationException with specific status, use it
        if (exception instanceof WebApplicationException) {
            WebApplicationException webEx = (WebApplicationException) exception;
            Response originalResponse = webEx.getResponse();
            
            // If response already has entity, return as is
            if (originalResponse.hasEntity()) {
                return originalResponse;
            }
            
            // Otherwise create error response
            Error error = new Error(
                    originalResponse.getStatus(),
                    "Request processing failed",
                    exception.getMessage() != null ? exception.getMessage() : "Unknown error"
            );
            return Response.status(originalResponse.getStatus()).entity(error).build();
        }

        // Log the exception for debugging
        exception.printStackTrace();

        // Return 500 for unexpected errors
        Error error = new Error(
                500,
                "Internal server error",
                "An unexpected error occurred while processing the request"
        );
        return Response.status(500).entity(error).build();
    }
}