package com.musicband.grammy.service;

import com.musicband.grammy.client.MainApiClient;
import com.musicband.grammy.model.AddParticipantResponse;
import com.musicband.grammy.model.Participant;
import com.musicband.grammy.repository.ParticipantRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * Service for Participant business logic
 */
@ApplicationScoped
public class ParticipantService {

    @Inject
    private ParticipantRepository repository;

    @Inject
    private MainApiClient mainApiClient;

    /**
     * Add participant to band
     */
    public AddParticipantResponse addParticipantToBand(Integer bandId, @Valid @NotNull Participant participant) {
        // Check if band exists
        if (!mainApiClient.bandExists(bandId)) {
            throw new IllegalArgumentException("Band with id " + bandId + " not found");
        }

        // Set band ID and create participant
        participant.setBandId(bandId);
        participant.setId(null);
        Participant created = repository.create(participant);

        // Count total participants for this band
        long participantsCount = repository.countByBandId(bandId);

        // Update numberOfParticipants in Main API
        boolean updated = mainApiClient.updateParticipantsCount(bandId, (int) participantsCount);
        if (!updated) {
            throw new RuntimeException("Failed to update participants count in Main API");
        }

        // Get band name for response
        String bandName = mainApiClient.getBandName(bandId);

        // Build response
        return new AddParticipantResponse(created, (int) participantsCount, bandId, bandName);
    }
}