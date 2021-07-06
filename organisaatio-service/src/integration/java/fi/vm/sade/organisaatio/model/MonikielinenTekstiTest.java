/*
 * Copyright (c) 2012 The Finnish Board of Education - Opetushallitus
 *
 * This program is free software:  Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://www.osor.eu/eupl/
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * European Union Public Licence for more details.
 */
package fi.vm.sade.organisaatio.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests to verify the translated text functionality works as expected.
 */
@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
public class MonikielinenTekstiTest {

    private static final Logger LOG = LoggerFactory.getLogger(MonikielinenTekstiTest.class);

    private static final String TEXT_FI1 = "fi1";
    private static final String TEXT_FI2 = "fi2";
    private static final String TEXT_EN1 = "en1";
    private static final String TEXT_EN2 = "en2";
    private static final String TEXT_SV1 = "hejssan";

    @PersistenceContext
    private EntityManager em;

    @BeforeEach
    public void getEm() {
        LOG.debug("getEm(): {}", em);
        assertNotNull(em);
    }

    @Test
    public void testInsertAndUpdate() {
        LOG.debug("testInsertAndUpdate()");

        Long countMt = getCountMonikielinenTeksti();
        LOG.debug("count mt = {}", countMt);

        MonikielinenTeksti mt = new MonikielinenTeksti();
        mt.addString("es", "hola!");
        mt.addString("fi", TEXT_FI1);
        mt.addString("en", TEXT_EN1);
        em.persist(mt);

        // Make sure it was inserted
        assertEquals((long) getCountMonikielinenTeksti(), (countMt + 1), "Count mt was not increased.");

        LOG.debug("count mt = {}", getCountMonikielinenTeksti());

        // Reload
        LOG.debug("  reload...");

        mt = findById(mt.getId());
        assertNotNull(mt);
        assertEquals(mt.getValues().size(), 3);

        // Verify loaded values are the same
        LOG.debug("  verify values...");

        assertEquals(TEXT_FI1, mt.getString("fi"));
        assertEquals(TEXT_EN1, mt.getString("en"));
        assertNotNull(mt.getString("es"));
        assertNull(mt.getString("ch"));

        // Update
        LOG.debug("  update...");

        mt.addString("en", TEXT_EN2);
        mt.addString("fi", TEXT_FI2);
        mt.addString("sv", TEXT_SV1);
        em.persist(mt);

        // Reload
        LOG.debug("  reload...");

        mt = findById(mt.getId());
        assertNotNull(mt);
        assertEquals(mt.getValues().size(), 4);

        // Verify loaded values are the same
        LOG.debug("  verify values...");

        assertEquals(TEXT_FI2, mt.getString("fi"));
        assertEquals(TEXT_EN2, mt.getString("en"));
        assertEquals(TEXT_SV1, mt.getString("sv"));
        assertNull(mt.getString("ch"));

        // Test removing translation koodiValue
        LOG.debug("  remove translation for sv...");

        // Remove two translations
        mt.addString("sv", null);
        mt.addString("en", null);
        em.persist(mt);

        // Reload
        LOG.debug("  reload...");

        mt = findById(mt.getId());
        assertNotNull(mt);
        assertEquals(mt.getValues().size(), 2);

        // Verify loaded values are the same
        LOG.debug("  verify values...");

        assertEquals(TEXT_FI2, mt.getString("fi"));
        assertNull(mt.getString("ch"));
        assertNull(mt.getString("sv"));
        assertNull(mt.getString("en"));
        assertNotNull(mt.getString("es"));
    }


    private MonikielinenTeksti findById(Long id) {
        MonikielinenTeksti mt = em.createQuery("FROM MonikielinenTeksti WHERE id = :id", MonikielinenTeksti.class).setParameter("id", id).getSingleResult();
        print(mt);
        return mt;
    }

    private Long getCountMonikielinenTeksti() {
        return (Long) em.createQuery("SELECT count(mt) FROM MonikielinenTeksti mt").getSingleResult();
    }

    private void print(MonikielinenTeksti mt) {
        LOG.debug("MT={}", mt);
        if (mt != null) {
            LOG.debug("  values={}", mt.getValues());
        }
    }
}
