package com.musicband.grammy.exception;

import com.musicband.grammy.model.Error;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class GeneralExceptionMapper implements ExceptionMapper<Exception> {
    @Override
    public Response toResponse(Exception e) {
        e.printStackTrace();
        Error error = new Error(500, "Internal error", e.getMessage());
        return Response.status(500).entity(error).build();
    }
}