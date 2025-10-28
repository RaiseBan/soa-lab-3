package com.musicband.api.exception;

import com.musicband.api.model.Error;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.sql.SQLException;

@Provider
public class SQLExceptionMapper implements ExceptionMapper<SQLException> {

    @Override
    public Response toResponse(SQLException exception) {
        exception.printStackTrace();
        
        String message = "Database operation failed";
        String details = "An error occurred while processing your request";
        
        if (exception.getMessage() != null) {
            String errorMsg = exception.getMessage().toLowerCase();
            
            if (errorMsg.contains("too long") || errorMsg.contains("data truncation")) {
                details = "Data is too long for database field. Please ensure all text fields are under 255 characters.";
            } else if (errorMsg.contains("duplicate") || errorMsg.contains("unique constraint")) {
                details = "This record already exists in the database";
            }
        }

        Error error = new Error(422, message, details);
        return Response.status(422).entity(error).build();
    }
}
