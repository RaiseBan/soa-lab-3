package com.musicband.grammy.resource;

import com.musicband.grammy.model.*;
import com.musicband.grammy.model.Error;

import com.musicband.grammy.service.ParticipantService;
import com.musicband.grammy.service.SingleService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;


@Path("/band")
public class GrammyResource {

    @Inject
    private SingleService singleService;

    @Inject
    private ParticipantService participantService;


    @POST
    @Path("/{band-id}/singles/add")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    public Response addSingleToBand(
            @PathParam("band-id") Integer bandId,
            @Valid Single single) {  // <-- ДОБАВЬ @Valid ТУТ!
        System.out.println("===addSingleToBand===");
        try {
            if (bandId == null || bandId < 1) {
                return createErrorResponse(422, "Validation failed",
                        "Band ID must be a positive integer");
            }

            if (single == null) {
                return createErrorResponse(400, "Invalid request body",
                        "Request body cannot be null");
            }

            AddSingleResponse response = singleService.addSingleToBand(bandId, single);
            return Response.status(Response.Status.CREATED).entity(response).build();

        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("not found")) {
                return createErrorResponse(404, "Music band not found", e.getMessage());
            }
            return createErrorResponse(422, "Business validation failed", e.getMessage());
        } catch (RuntimeException e) {
            e.printStackTrace();
            if (e.getMessage() != null && e.getMessage().contains("Main API service unavailable")) {
                return createErrorResponse(503, "Main API service unavailable",
                        "Unable to connect to Music Band Management API");
            }
            return createErrorResponse(500, "Internal server error",
                    "An unexpected error occurred while processing the request");
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse(500, "Internal server error",
                    "An unexpected error occurred while processing the request");
        }
    }

    @POST
    @Path("/{band-id}/participants/add")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    public Response addParticipantToBand(
            @PathParam("band-id") Integer bandId,
            @Valid Participant participant) {  // <-- ДОБАВЬ @Valid И ТУТ!

        try {
            if (bandId == null || bandId < 1) {
                return createErrorResponse(422, "Validation failed",
                        "Band ID must be a positive integer");
            }

            if (participant == null) {
                return createErrorResponse(400, "Invalid request body",
                        "Request body cannot be null");
            }

            AddParticipantResponse response = participantService.addParticipantToBand(bandId, participant);
            return Response.status(Response.Status.CREATED).entity(response).build();

        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("not found")) {
                return createErrorResponse(404, "Music band not found", e.getMessage());
            }
            return createErrorResponse(422, "Business validation failed", e.getMessage());
        } catch (RuntimeException e) {
            e.printStackTrace();
            if (e.getMessage() != null && e.getMessage().contains("Main API service unavailable")) {
                return createErrorResponse(503, "Main API service unavailable",
                        "Unable to connect to Music Band Management API");
            }
            return createErrorResponse(500, "Internal server error",
                    "An unexpected error occurred while processing the request");
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse(500, "Internal server error",
                    "An unexpected error occurred while processing the request");
        }
    }

    
    private Response createErrorResponse(int code, String message, String details) {
        Error error = new Error(code, message, details);
        Response.Status status;

        switch (code) {
            case 400:
                status = Response.Status.BAD_REQUEST;
                break;
            case 404:
                status = Response.Status.NOT_FOUND;
                break;
            case 422:
                status = Response.Status.fromStatusCode(422);
                break;
            case 503:
                status = Response.Status.SERVICE_UNAVAILABLE;
                break;
            default:
                status = Response.Status.INTERNAL_SERVER_ERROR;
        }

        return Response.status(status).entity(error).build();
    }
}