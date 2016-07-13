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
package fi.vm.sade.organisaatio.service;

import fi.vm.sade.organisaatio.business.exception.OrganisaatioDateException;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map.Entry;

import org.junit.Assert;

import org.junit.Test;

import com.google.common.collect.Maps;

import fi.vm.sade.organisaatio.model.Organisaatio;

import static org.junit.Assert.*;

public class OrganisationDateValidatorTest {

	private static Date date(int n) {
		return new GregorianCalendar(2000+n, 0, 0).getTime();
	}
    private final OrganisationDateValidator validator = new OrganisationDateValidator(false);
    private final OrganisationDateValidator validatorSkipStartDate = new OrganisationDateValidator(true);
    private Organisaatio parent = new Organisaatio();
    private Organisaatio child = new Organisaatio();
    private final Entry<Organisaatio, Organisaatio> parentChild = Maps.immutableEntry(parent, child);
	
    @Test
    public void testValidator() {

        // both parent and child null
        assertTrue(validator.apply(Maps.immutableEntry((Organisaatio) null, (Organisaatio) null)));

        // parent or child null
        assertTrue(validator.apply(Maps.immutableEntry(parent, (Organisaatio) null)));
        assertTrue(validator.apply(Maps.immutableEntry((Organisaatio) null, child)));

        // all dates null
        assertTrue(validator.apply(parentChild));

        // parent has end date, child does not
        parent.setLakkautusPvm(date(100));
        assertNotEquals(parent.getLakkautusPvm(), child.getLakkautusPvm());
        // side effect happends during validation
        assertTrue(validator.apply(parentChild));

        // side effect result
        assertEquals(parent.getLakkautusPvm(), child.getLakkautusPvm());

        // validates ok after side effect?
        assertTrue(validator.apply(parentChild));

        // parent has start date
        parent.setAlkuPvm(date(10));
        assertTrue(validator.apply(parentChild));

        // both have same start date
        child.setAlkuPvm(date(10));
        assertTrue(validator.apply(parentChild));

        // alku < parent.alku
        child.setAlkuPvm(date(9));
        assertFalse(validator.apply(parentChild));

        // alku < parent alku ok if validator skips start date validation
        assertTrue(validatorSkipStartDate.apply(parentChild));

        // alku > parent.alku
        child.setAlkuPvm(date(11));
        // THIS IS ALWAYS PERMITTED
        assertTrue(validator.apply(parentChild));
        assertTrue(validatorSkipStartDate.apply(parentChild));

        // loppu > parent.loppu
        child.setAlkuPvm(date(10));
        child.setLakkautusPvm(date(101));
        assertFalse(validator.apply(parentChild));
        assertFalse(validatorSkipStartDate.apply(parentChild));

        // parent has no end date, child has
        parent.setLakkautusPvm(null);
        assertTrue(validator.apply(parentChild));

        // parent has no start date, child has
        parent.setAlkuPvm(null);
        assertTrue(validator.apply(parentChild));
    }

    @Test(expected = OrganisaatioDateException.class)
    public void childBeginningAfterParentEndFails() {
        // child start date after parent end date
        child.setAlkuPvm(date(5));
        parent.setLakkautusPvm(date(1));
        validator.apply(parentChild);
    }

    @Test(expected = OrganisaatioDateException.class)
    public void childBeginningAfterParentEndFailsWithoutStartDateValidation() {
        child.setAlkuPvm(date(5));
        parent.setLakkautusPvm(date(1));
        validatorSkipStartDate.apply(parentChild);
    }

}
