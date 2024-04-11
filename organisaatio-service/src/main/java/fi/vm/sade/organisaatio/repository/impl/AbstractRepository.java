package fi.vm.sade.organisaatio.repository.impl;

import com.querydsl.jpa.JPQLTemplates;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

public abstract class AbstractRepository {
    @PersistenceContext
    protected EntityManager em;

    protected JPAQueryFactory jpa() {
        return new JPAQueryFactory(JPQLTemplates.DEFAULT, em);
    }
}
