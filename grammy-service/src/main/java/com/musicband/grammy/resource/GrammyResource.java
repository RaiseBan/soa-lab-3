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

/**
 * REST Resource for Grammy Service endpoints
 */
@Path("/band")
public class GrammyResource {

    @Inject
    private SingleService singleService;

    @Inject
    private ParticipantService participantService;

    /**
     * POST /band/{band-id}/singles/add - Add single to band
     */
    @POST
    @Path("/{band-id}/singles/add")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    public Response addSingleToBand(
            @PathParam("band-id") Integer bandId,
            Single single) {
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
        } catch (Exception e) {
            if (e.getMessage().contains("Main API")) {
                return createErrorResponse(503, "Main API service unavailable",
                        "Unable to connect to Music Band Management API at http://helios:8080/api/v1");
            }
            return createErrorResponse(500, "Internal server error",
                    "An unexpected error occurred: " + e.getMessage());
        }
    }

    /**
     * POST /band/{band-id}/participants/add - Add participant to band
     */
    @POST
    @Path("/{band-id}/participants/add")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    public Response addParticipantToBand(
            @PathParam("band-id") Integer bandId,
            Participant participant) {

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
        } catch (Exception e) {
            e.printStackTrace();
            if (e.getMessage().contains("Main API")) {
                return createErrorResponse(503, "Main API service unavailable",
                        "Unable to connect to Music Band Management API at http://helios:8080/api/v1");
            }
            return createErrorResponse(500, "Internal server error",
                    "An unexpected error occurred: " + e.getMessage());
        }
    }

    /**
     * Helper method to create error responses
     */
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