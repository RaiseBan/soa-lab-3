package com.musicband.api.service;

import com.musicband.api.model.AverageParticipantsResponse;
import com.musicband.api.model.BandsResponse;
import com.musicband.api.model.MusicBand;
import jakarta.ejb.Remote;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;

@Remote
public interface MusicBandServiceRemote {

    BandsResponse getAllBands(int page, int size, List<String> sortFields, Map<String, String> filters);

    MusicBand getBandById(Integer id);  

    MusicBand createBand(@Valid @NotNull MusicBand band);

    MusicBand updateBand(Integer id, @Valid @NotNull MusicBand updatedBand);  

    MusicBand patchBand(Integer id, MusicBand patchData);  

    boolean deleteBand(Integer id);

    AverageParticipantsResponse getAverageParticipants();
}