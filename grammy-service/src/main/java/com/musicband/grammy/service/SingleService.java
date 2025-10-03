package com.musicband.grammy.service;

import com.musicband.grammy.client.MainApiClient;
import com.musicband.grammy.model.AddSingleResponse;
import com.musicband.grammy.model.Single;
import com.musicband.grammy.repository.SingleRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;


@ApplicationScoped
public class SingleService {

    @Inject
    private SingleRepository repository;

    @Inject
    private MainApiClient mainApiClient;

    
    public AddSingleResponse addSingleToBand(Integer bandId, @Valid @NotNull Single single) {
        
        if (!mainApiClient.bandExists(bandId)) {
            throw new IllegalArgumentException("Band with id " + bandId + " not found");
        }

        
        single.setBandId(bandId);
        single.setId(null);
        Single created = repository.create(single);

        
        String bandName = mainApiClient.getBandName(bandId);
        System.out.println("BandName: " + bandName);

        
        return new AddSingleResponse(created, bandId, bandName);
    }
}