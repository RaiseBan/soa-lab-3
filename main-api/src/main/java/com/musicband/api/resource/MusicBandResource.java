package com.musicband.api.resource;

import com.musicband.api.model.AverageParticipantsResponse;
import com.musicband.api.model.BandsResponse;
import com.musicband.api.model.Error;
import com.musicband.api.model.MusicBand;
import com.musicband.api.service.MusicBandService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.*;

/**
 * REST Resource for MusicBand endpoints
 */
@Path("/bands")
public class MusicBandResource {

    @Inject
    private MusicBandService service;

    /**
     * GET /bands - Get list of bands with pagination, filtering and sorting
     */
    @GET
    @Produces(MediaType.APPLICATION_XML)
    public Response getBands(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("sort") List<String> sort,
            @QueryParam("filter") List<String> filter) {

        try {
            if (page < 0) {
                return createErrorResponse(422, "Validation failed",
                        "Page number must be non-negative");
            }

            if (size < 1 || size > 100) {
                return createErrorResponse(422, "Validation failed",
                        "Page size must be between 1 and 100");
            }

            Map<String, String> filters = new HashMap<>();
            if (filter != null) {
                for (int i = 0; i < filter.size(); i++) {
                    filters.put("filter" + i, filter.get(i));
                }
            }

            BandsResponse response = service.getAllBands(page, size, sort, filters);


            System.out.println("=== DEBUG Bands Response ===");
            System.out.println("Total bands: " + response.getBands().size());
            System.out.println("Total elements: " + response.getTotalElements());

            for (int i = 0; i < response.getBands().size(); i++) {
                MusicBand band = response.getBands().get(i);
                System.out.println("Band " + i + ":");
                System.out.println("  ID: " + band.getId());
                System.out.println("  Name: " + band.getName());
                System.out.println("  CreationDate: " + band.getCreationDate());
                System.out.println("  CreationDate is null: " + (band.getCreationDate() == null));
                if (band.getCreationDate() != null) {
                    System.out.println("  CreationDate class: " + band.getCreationDate().getClass());
                    System.out.println("  CreationDate string: " + band.getCreationDate().toString());
                }
            }

            return Response.ok(response).build();

        } catch (IllegalArgumentException e) {
            return createErrorResponse(422, "Validation failed", e.getMessage());
        } catch (Exception e) {
            return createErrorResponse(500, "Internal server error",
                    "An unexpected error occurred: " + e.getMessage());
        }
    }

    /**
     * POST /bands - Create new band
     */
    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    public Response createBand(@Valid MusicBand band) {
        try {
            if (band == null) {
                return createErrorResponse(400, "Invalid request body",
                        "Request body cannot be null");
            }

            MusicBand created = service.createBand(band);
            return Response.status(Response.Status.CREATED).entity(created).build();

        } catch (IllegalArgumentException e) {
            return createErrorResponse(422, "Validation failed", e.getMessage());
        } catch (Exception e) {
            return createErrorResponse(500, "Internal server error",
                    "An unexpected error occurred: " + e.getMessage());
        }
    }

    /**
     * GET /bands/{id} - Get band by ID
     */
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_XML)
    public Response getBandById(@PathParam("id") Integer id) {
        try {
            if (id == null || id < 1) {
                return createErrorResponse(422, "Validation failed",
                        "Band ID must be a positive integer");
            }

            Optional<MusicBand> band = service.getBandById(id);

            if (band.isEmpty()) {
                return createErrorResponse(404, "Resource not found",
                        "MusicBand with id " + id + " not found");
            }

            return Response.ok(band.get()).build();

        } catch (Exception e) {
            return createErrorResponse(500, "Internal server error",
                    "An unexpected error occurred: " + e.getMessage());
        }
    }

    /**
     * PUT /bands/{id} - Update band (full update)
     */
    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    public Response updateBand(@PathParam("id") Integer id, @Valid MusicBand band) {
        try {
            if (id == null || id < 1) {
                return createErrorResponse(422, "Validation failed",
                        "Band ID must be a positive integer");
            }

            if (band == null) {
                return createErrorResponse(400, "Invalid request body",
                        "Request body cannot be null");
            }

            Optional<MusicBand> updated = service.updateBand(id, band);

            if (updated.isEmpty()) {
                return createErrorResponse(404, "Resource not found",
                        "MusicBand with id " + id + " not found");
            }

            return Response.ok(updated.get()).build();

        } catch (IllegalArgumentException e) {
            return createErrorResponse(422, "Validation failed", e.getMessage());
        } catch (Exception e) {
            return createErrorResponse(500, "Internal server error",
                    "An unexpected error occurred: " + e.getMessage());
        }
    }

    /**
     * PATCH /bands/{id} - Partially update band
     */
    @PATCH
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    public Response patchBand(@PathParam("id") Integer id, MusicBand patchData) {
        try {
            if (id == null || id < 1) {
                return createErrorResponse(422, "Validation failed",
                        "Band ID must be a positive integer");
            }

            if (patchData == null) {
                return createErrorResponse(400, "Invalid request body",
                        "Request body cannot be null");
            }

            Optional<MusicBand> patched = service.patchBand(id, patchData);

            if (patched.isEmpty()) {
                return createErrorResponse(404, "Resource not found",
                        "MusicBand with id " + id + " not found");
            }

            return Response.ok(patched.get()).build();

        } catch (IllegalArgumentException e) {
            return createErrorResponse(422, "Validation failed", e.getMessage());
        } catch (Exception e) {
            return createErrorResponse(500, "Internal server error",
                    "An unexpected error occurred: " + e.getMessage());
        }
    }

    /**
     * DELETE /bands/{id} - Delete band
     */
    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_XML)
    public Response deleteBand(@PathParam("id") Integer id) {
        try {
            if (id == null || id < 1) {
                return createErrorResponse(422, "Validation failed",
                        "Band ID must be a positive integer");
            }

            boolean deleted = service.deleteBand(id);

            if (!deleted) {
                return createErrorResponse(404, "Resource not found",
                        "MusicBand with id " + id + " not found");
            }

            return Response.noContent().build();

        } catch (Exception e) {
            return createErrorResponse(500, "Internal server error",
                    "An unexpected error occurred: " + e.getMessage());
        }
    }

    /**
     * GET /bands/statistics/average-participants - Get average participants
     */
    @GET
    @Path("/statistics/average-participants")
    @Produces(MediaType.APPLICATION_XML)
    public Response getAverageParticipants() {
        try {
            AverageParticipantsResponse response = service.getAverageParticipants();
            return Response.ok(response).build();

        } catch (Exception e) {
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
            default:
                status = Response.Status.INTERNAL_SERVER_ERROR;
        }

        return Response.status(status).entity(error).build();
    }
}