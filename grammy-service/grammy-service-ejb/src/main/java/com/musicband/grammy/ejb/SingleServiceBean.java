package com.musicband.grammy.ejb;

import com.musicband.grammy.client.MainApiClient;
import com.musicband.grammy.model.AddSingleResponse;
import com.musicband.grammy.model.Single;
import com.musicband.grammy.repository.SingleRepository;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

@Stateless
public class SingleServiceBean implements SingleServiceRemote {

    @Inject
    private SingleRepository repository;

    @Inject
    private MainApiClient mainApiClient;

    @Override
    public AddSingleResponse addSingleToBand(Integer bandId, Single single) {
        // Проверяем, существует ли группа
        if (!mainApiClient.bandExists(bandId)) {
            throw new IllegalArgumentException("Band with id " + bandId + " not found");
        }

        // Устанавливаем bandId и очищаем id для создания нового single
        single.setBandId(bandId);
        single.setId(null);
        Single created = repository.create(single);

        // Получаем имя группы
        String bandName = mainApiClient.getBandName(bandId);
        System.out.println("BandName: " + bandName);

        // Возвращаем ответ
        return new AddSingleResponse(created, bandId, bandName);
    }
}
