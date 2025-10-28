package com.musicband.grammy.exception;

import com.musicband.grammy.model.Error;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.sql.DataTruncation;

@Provider
public class DataTruncationExceptionMapper implements ExceptionMapper<DataTruncation> {

    @Override
    public Response toResponse(DataTruncation exception) {
        Error error = new Error(
                422,
                "Validation failed",
                "Data is too long for database field. Please ensure all text fields are under 255 characters."
        );

        return Response.status(422).entity(error).build();
    }
}
