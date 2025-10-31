package com.musicband.grammy.ejb;

import com.musicband.grammy.client.MainApiClient;
import com.musicband.grammy.model.AddParticipantResponse;
import com.musicband.grammy.model.Participant;
import com.musicband.grammy.repository.ParticipantRepository;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import org.jboss.ejb3.annotation.Pool;

@Stateless
@Pool("slsb-strict-max-pool")
public class ParticipantServiceBean implements ParticipantServiceRemote {
    @Inject
    private ParticipantRepository repository;

    @Inject
    private MainApiClient mainApiClient;

    @Override
    public AddParticipantResponse addParticipantToBand(Integer bandId, Participant participant) {
        
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
