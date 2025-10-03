package com.musicband.grammy.repository;

import com.musicband.grammy.model.Participant;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;


@ApplicationScoped
@Transactional
public class ParticipantRepository {

    @PersistenceContext(unitName = "grammyPU")
    private EntityManager entityManager;

    public Participant create(Participant participant) {
        entityManager.persist(participant);
        entityManager.flush();
        return participant;
    }

    public long countByBandId(Integer bandId) {
        return entityManager.createQuery(
            "SELECT COUNT(p) FROM Participant p WHERE p.bandId = :bandId", Long.class)
            .setParameter("bandId", bandId)
            .getSingleResult();
    }
}