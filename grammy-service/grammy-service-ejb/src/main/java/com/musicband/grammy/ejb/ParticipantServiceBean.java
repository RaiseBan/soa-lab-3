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
        // Проверяем, существует ли группа
        if (!mainApiClient.bandExists(bandId)) {
            throw new IllegalArgumentException("Band with id " + bandId + " not found");
        }

        // Устанавливаем bandId и очищаем id
        participant.setBandId(bandId);
        participant.setId(null);
        Participant created = repository.create(participant);

        // Подсчитываем участников
        long participantsCount = repository.countByBandId(bandId);

        // Обновляем количество участников в Main API
        boolean updated = mainApiClient.updateParticipantsCount(bandId, (int) participantsCount);
        if (!updated) {
            throw new RuntimeException("Failed to update participants count in Main API");
        }

        // Получаем имя группы
        String bandName = mainApiClient.getBandName(bandId);

        // Возвращаем ответ
        return new AddParticipantResponse(created, (int) participantsCount, bandId, bandName);
    }
}
