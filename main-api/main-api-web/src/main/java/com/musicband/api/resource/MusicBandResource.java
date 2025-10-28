package com.musicband.api.resource;

import com.musicband.api.model.*;
import com.musicband.api.service.MusicBandService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.*;

@Path("/bands")
public class MusicBandResource {

    @Inject
    private MusicBandService service;

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
            return Response.ok(response).build();

        } catch (IllegalArgumentException e) {
            return createErrorResponse(422, "Validation failed", e.getMessage());
        } catch (Exception e) {
            return createErrorResponse(500, "Internal server error",
                    "An unexpected error occurred: " + e.getMessage());
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    public Response createBand(String xmlBody) {
        try {
            if (xmlBody == null || xmlBody.trim().isEmpty()) {
                return createErrorResponse(400, "Invalid request body",
                        "Request body cannot be null or empty");
            }

            int count = countOccurrences(xmlBody, "<musicBand>");
            if (count > 1) {
                return createErrorResponse(422, "Multiple bands not allowed",
                        "Single band creation endpoint accepts only one musicBand element. ");
            }
            if (count == 0) {
                return createErrorResponse(400, "Invalid request body",
                        "Request body must contain a <musicBand> element");
            }

            jakarta.xml.bind.JAXBContext jaxbContext = jakarta.xml.bind.JAXBContext.newInstance(MusicBand.class);
            jakarta.xml.bind.Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            java.io.StringReader reader = new java.io.StringReader(xmlBody);
            MusicBand band = (MusicBand) unmarshaller.unmarshal(reader);

            jakarta.validation.ValidatorFactory factory = jakarta.validation.Validation.buildDefaultValidatorFactory();
            jakarta.validation.Validator validator = factory.getValidator();
            Set<jakarta.validation.ConstraintViolation<MusicBand>> violations = validator.validate(band);

            if (!violations.isEmpty()) {
                String violationMessages = violations.stream()
                        .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                        .reduce((a, b) -> a + "; " + b)
                        .orElse("Validation failed");
                return createErrorResponse(422, "Validation failed", violationMessages);
            }

            MusicBand created = service.createBand(band);
            return Response.status(Response.Status.CREATED).entity(created).build();

        } catch (jakarta.xml.bind.JAXBException e) {
            return createErrorResponse(400, "Invalid XML format",
                    "Failed to parse XML: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return createErrorResponse(422, "Validation failed", e.getMessage());
        } catch (Exception e) {
            return createErrorResponse(500, "Internal server error",
                    "An unexpected error occurred: " + e.getMessage());
        }
    }

    private int countOccurrences(String text, String pattern) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(pattern, index)) != -1) {
            count++;
            index += pattern.length();
        }
        return count;
    }

    @POST
    @Path("/bulk")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    public Response createBands(BulkBandsRequest bulkRequest) {
        try {
            if (bulkRequest == null || bulkRequest.getBands() == null || bulkRequest.getBands().isEmpty()) {
                return createErrorResponse(400, "Invalid request body",
                        "Request body must contain at least one band");
            }

            List<MusicBand> createdBands = new ArrayList<>();
            List<String> errors = new ArrayList<>();

            for (int i = 0; i < bulkRequest.getBands().size(); i++) {
                MusicBand band = bulkRequest.getBands().get(i);
                try {
                    MusicBand created = service.createBand(band);
                    createdBands.add(created);
                } catch (Exception e) {
                    errors.add("Band #" + (i + 1) + ": " + e.getMessage());
                }
            }

            BulkBandsResponse response = new BulkBandsResponse();
            response.setSuccessful(createdBands);
            response.setErrors(errors);
            response.setTotalProcessed(bulkRequest.getBands().size());
            response.setSuccessCount(createdBands.size());
            response.setErrorCount(errors.size());

            if (createdBands.isEmpty()) {
                return Response.status(422).entity(response).build();
            }

            return Response.status(Response.Status.CREATED).entity(response).build();

        } catch (Exception e) {
            return createErrorResponse(500, "Internal server error",
                    "An unexpected error occurred: " + e.getMessage());
        }
    }

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

    private Response createErrorResponse(int code, String message, String details) {
        Error error = new Error(code, message, details);
        return Response.status(code).entity(error).build();
    }
}
