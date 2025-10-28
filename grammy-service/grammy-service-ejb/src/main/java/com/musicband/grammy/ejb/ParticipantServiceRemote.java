package com.musicband.grammy.ejb;

import com.musicband.grammy.model.AddParticipantResponse;
import com.musicband.grammy.model.Participant;
import jakarta.ejb.Remote;

@Remote
public interface ParticipantServiceRemote {
    
    AddParticipantResponse addParticipantToBand(Integer bandId, Participant participant);
}
