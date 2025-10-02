package com.musicband.grammy.service;

import com.musicband.grammy.client.MainApiClient;
import com.musicband.grammy.model.AddSingleResponse;
import com.musicband.grammy.model.Single;
import com.musicband.grammy.repository.SingleRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * Service for Single business logic
 */
@ApplicationScoped
public class SingleService {

    @Inject
    private SingleRepository repository;

    @Inject
    private MainApiClient mainApiClient;

    /**
     * Add single to band
     */
    public AddSingleResponse addSingleToBand(Integer bandId, @Valid @NotNull Single single) {
        // Check if band exists
        if (!mainApiClient.bandExists(bandId)) {
            throw new IllegalArgumentException("Band with id " + bandId + " not found");
        }

        // Set band ID and create single
        single.setBandId(bandId);
        single.setId(null);
        Single created = repository.create(single);

        // Get band name for response
        String bandName = mainApiClient.getBandName(bandId);
        System.out.println("BandName: " + bandName);

        // Build response
        return new AddSingleResponse(created, bandId, bandName);
    }
}