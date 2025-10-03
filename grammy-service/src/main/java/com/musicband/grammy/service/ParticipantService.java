package com.musicband.grammy.service;

import com.musicband.grammy.client.MainApiClient;
import com.musicband.grammy.model.AddParticipantResponse;
import com.musicband.grammy.model.Participant;
import com.musicband.grammy.repository.ParticipantRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;


@ApplicationScoped
public class ParticipantService {

    @Inject
    private ParticipantRepository repository;

    @Inject
    private MainApiClient mainApiClient;

    
    public AddParticipantResponse addParticipantToBand(Integer bandId, @Valid @NotNull Participant participant) {
        
        if (!mainApiClient.bandExists(bandId)) {
            throw new IllegalArgumentException("Band with id " + bandId + " not found");
        }

        
        participant.setBandId(bandId);
        participant.setId(null);
        Participant created = repository.create(participant);

        
        long participantsCount = repository.countByBandId(bandId);

        
        boolean updated = mainApiClient.updateParticipantsCount(bandId, (int) participantsCount);
        if (!updated) {
            throw new RuntimeException("Failed to update participants count in Main API");
        }

        
        String bandName = mainApiClient.getBandName(bandId);

        
        return new AddParticipantResponse(created, (int) participantsCount, bandId, bandName);
    }
}