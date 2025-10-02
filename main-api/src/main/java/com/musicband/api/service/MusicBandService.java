package com.musicband.api.service;

import com.musicband.api.model.AverageParticipantsResponse;
import com.musicband.api.model.BandsResponse;
import com.musicband.api.model.MusicBand;
import com.musicband.api.repository.MusicBandRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for MusicBand business logic
 */
@ApplicationScoped
public class MusicBandService {

    @Inject
    private MusicBandRepository repository;

    /**
     * Get paginated list of bands with filtering and sorting
     */
    public BandsResponse getAllBands(int page, int size, List<String> sortFields, Map<String, String> filters) {
        List<MusicBand> bands = repository.findAll(page, size, sortFields, filters);
        long totalElements = repository.count(filters);
        int totalPages = (int) Math.ceil((double) totalElements / size);

        return new BandsResponse(bands, totalElements, totalPages, page, size);
    }

    /**
     * Get band by ID
     */
    public Optional<MusicBand> getBandById(Integer id) {
        return repository.findById(id);
    }

    /**
     * Create new band
     */
    public MusicBand createBand(@Valid @NotNull MusicBand band) {
        // ID and creationDate will be set automatically
        band.setId(null);
        return repository.create(band);
    }

    /**
     * Update existing band (full update)
     */
    public Optional<MusicBand> updateBand(Integer id, @Valid @NotNull MusicBand updatedBand) {
        Optional<MusicBand> existing = repository.findById(id);
        
        if (existing.isEmpty()) {
            return Optional.empty();
        }

        MusicBand band = existing.get();
        
        // Update all fields except id and creationDate
        band.setName(updatedBand.getName());
        band.setCoordinates(updatedBand.getCoordinates());
        band.setNumberOfParticipants(updatedBand.getNumberOfParticipants());
        band.setAlbumsCount(updatedBand.getAlbumsCount());
        band.setGenre(updatedBand.getGenre());
        band.setLabel(updatedBand.getLabel());

        return Optional.of(repository.update(band));
    }

    /**
     * Partially update band (patch)
     */
    public Optional<MusicBand> patchBand(Integer id, MusicBand patchData) {
        Optional<MusicBand> existing = repository.findById(id);
        
        if (existing.isEmpty()) {
            return Optional.empty();
        }

        MusicBand band = existing.get();
        
        // Update only provided fields
        if (patchData.getName() != null && !patchData.getName().isBlank()) {
            band.setName(patchData.getName());
        }
        
        if (patchData.getCoordinates() != null) {
            band.setCoordinates(patchData.getCoordinates());
        }
        
        if (patchData.getNumberOfParticipants() != null) {
            band.setNumberOfParticipants(patchData.getNumberOfParticipants());
        }
        
        if (patchData.getAlbumsCount() != null) {
            band.setAlbumsCount(patchData.getAlbumsCount());
        }
        
        if (patchData.getGenre() != null) {
            band.setGenre(patchData.getGenre());
        }
        
        if (patchData.getLabel() != null) {
            band.setLabel(patchData.getLabel());
        }

        return Optional.of(repository.update(band));
    }

    /**
     * Delete band by ID
     */
    public boolean deleteBand(Integer id) {
        return repository.delete(id);
    }

    /**
     * Get average number of participants
     */
    public AverageParticipantsResponse getAverageParticipants() {
        Double average = repository.getAverageParticipants();
        long totalBands = repository.count(null);
        
        return new AverageParticipantsResponse(average, (int) totalBands);
    }
}