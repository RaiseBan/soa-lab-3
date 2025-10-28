package com.musicband.api.exception;

import com.musicband.api.model.Error;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;


@Provider
public class GeneralExceptionMapper implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception exception) {

        if (exception instanceof WebApplicationException) {
            WebApplicationException webEx = (WebApplicationException) exception;
            Response originalResponse = webEx.getResponse();

            if (originalResponse.hasEntity()) {
                return originalResponse;
            }

            Error error = new Error(
                    originalResponse.getStatus(),
                    "Request processing failed",
                    "The request could not be processed"
            );
            return Response.status(originalResponse.getStatus()).entity(error).build();
        }

        exception.printStackTrace();

        Error error = new Error(
                500,
                "Internal server error",
                "An unexpected error occurred while processing the request"
        );
        return Response.status(500).entity(error).build();
    }
}
