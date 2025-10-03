package com.musicband.grammy.repository;

import com.musicband.grammy.model.Single;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;


@ApplicationScoped
@Transactional
public class SingleRepository {

    @PersistenceContext(unitName = "grammyPU")
    private EntityManager entityManager;

    public Single create(Single single) {
        entityManager.persist(single);
        entityManager.flush();
        return single;
    }
}