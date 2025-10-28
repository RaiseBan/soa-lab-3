package com.musicband.api.service;

import com.musicband.api.model.AverageParticipantsResponse;
import com.musicband.api.model.BandsResponse;
import com.musicband.api.model.MusicBand;
import jakarta.ejb.Remote;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Remote
public interface MusicBandServiceRemote {
    
    BandsResponse getAllBands(int page, int size, List<String> sortFields, Map<String, String> filters);
    
    Optional<MusicBand> getBandById(Integer id);
    
    MusicBand createBand(MusicBand band);
    
    Optional<MusicBand> updateBand(Integer id, MusicBand updatedBand);
    
    Optional<MusicBand> patchBand(Integer id, MusicBand patchData);
    
    boolean deleteBand(Integer id);
    
    AverageParticipantsResponse getAverageParticipants();
}