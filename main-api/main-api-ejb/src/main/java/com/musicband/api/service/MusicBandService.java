package com.musicband.api.service;

import com.musicband.api.model.AverageParticipantsResponse;
import com.musicband.api.model.BandsResponse;
import com.musicband.api.model.MusicBand;
import com.musicband.api.repository.MusicBandRepository;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.jboss.ejb3.annotation.Pool;

@Stateless
@Pool("slsb-strict-max-pool")
public class MusicBandService implements MusicBandServiceRemote {

    @Inject
    private MusicBandRepository repository;

    @Override
    public BandsResponse getAllBands(int page, int size, List<String> sortFields, Map<String, String> filters) {
        List<MusicBand> bands = repository.findAll(page, size, sortFields, filters);
        long totalElements = repository.count(filters);
        int totalPages = (int) Math.ceil((double) totalElements / size);

        return new BandsResponse(bands, totalElements, totalPages, page, size);
    }

    @Override
    public Optional<MusicBand> getBandById(Integer id) {
        return repository.findById(id);
    }

    @Override
    public MusicBand createBand(MusicBand band) {
        band.setId(null);
        return repository.create(band);
    }

    @Override
    public Optional<MusicBand> updateBand(Integer id, MusicBand updatedBand) {
        Optional<MusicBand> existing = repository.findById(id);

        if (existing.isEmpty()) {
            return Optional.empty();
        }

        MusicBand band = existing.get();

        band.setName(updatedBand.getName());
        band.setCoordinates(updatedBand.getCoordinates());
        band.setNumberOfParticipants(updatedBand.getNumberOfParticipants());
        band.setAlbumsCount(updatedBand.getAlbumsCount());
        band.setGenre(updatedBand.getGenre());
        band.setLabel(updatedBand.getLabel());

        return Optional.of(repository.update(band));
    }

    @Override
    public Optional<MusicBand> patchBand(Integer id, MusicBand patchData) {
        Optional<MusicBand> existing = repository.findById(id);

        if (existing.isEmpty()) {
            return Optional.empty();
        }

        MusicBand band = existing.get();

        if (patchData.getName() != null) {
            if (patchData.getName().isBlank()) {
                throw new IllegalArgumentException("Name cannot be empty");
            }
            band.setName(patchData.getName());
        }

        if (patchData.getCoordinates() != null) {
            band.setCoordinates(patchData.getCoordinates());
        }

        if (patchData.getNumberOfParticipants() != null) {
            if (patchData.getNumberOfParticipants() < 1) {
                throw new IllegalArgumentException("Number of participants must be greater than 0");
            }
            band.setNumberOfParticipants(patchData.getNumberOfParticipants());
        }

        if (patchData.getAlbumsCount() != null) {
            if (patchData.getAlbumsCount() < 1) {
                throw new IllegalArgumentException("Albums count must be greater than 0");
            }
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

    @Override
    public boolean deleteBand(Integer id) {
        return repository.delete(id);
    }

    @Override
    public AverageParticipantsResponse getAverageParticipants() {
        Double average = repository.getAverageParticipants();
        long totalBands = repository.count(null);

        return new AverageParticipantsResponse(average, (int) totalBands);
    }
}