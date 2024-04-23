/*
 *
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

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import fi.vm.sade.organisaatio.business.exception.OrganisaatioDateException;
import fi.vm.sade.organisaatio.model.Organisaatio;

import jakarta.annotation.Nullable;
import java.util.Date;
import java.util.Map.Entry;

/**
 * Validates start and end dates for child and parent.
 */
public class OrganisationDateValidator implements Predicate<Entry<Organisaatio, Organisaatio>> {

    /**
     * Validate start dates are ok
     */
    // this is no longer used because moving/merging organisations often breaks this condition
    private Predicate<Entry<Organisaatio, Organisaatio>> startDateValidator = new Predicate<Entry<Organisaatio, Organisaatio>>() {

        @Override
        public boolean apply(Entry<Organisaatio, Organisaatio> parentChild) {
            Preconditions.checkNotNull(parentChild);
            if (parentChild.getKey() == null || parentChild.getValue() == null) {
                return true;
            }
            final Date parentStartDate = parentChild.getKey().getAlkuPvm();

            if (parentStartDate == null) {
                return true;
            }

            final Date startDate = parentChild.getValue().getAlkuPvm();
            if (startDate == null) {
                return true;
            }
            return !startDate.before(parentStartDate);
        }
    };

    /**
     * validate that end date => startdate
     */
    private Predicate<Entry<Organisaatio, Organisaatio>> dateValidator = new Predicate<Entry<Organisaatio, Organisaatio>>() {
        public boolean apply(java.util.Map.Entry<Organisaatio, Organisaatio> parentChild) {
            return parentChild.getValue()==null || validate(parentChild.getValue().getAlkuPvm(), parentChild.getValue().getLakkautusPvm());
        }

        private boolean validate(Date alkuPvm, Date lakkautusPvm) {
            if (lakkautusPvm != null && alkuPvm != null && lakkautusPvm.before(alkuPvm)) {
                throw new OrganisaatioDateException();
            }

            return true;
        }
    };

    /**
     * Validate end dates are ok.
     */
    private Predicate<Entry<Organisaatio, Organisaatio>> endDateValidator = new Predicate<Entry<Organisaatio, Organisaatio>>() {

        @Override
        public boolean apply(Entry<Organisaatio, Organisaatio> parentChild) {
            Preconditions.checkNotNull(parentChild);
            if (parentChild.getKey() == null || parentChild.getValue() == null) {
                return true;
            }

            final Date parentEndDate = parentChild.getKey().getLakkautusPvm();
            final Date endDate = parentChild.getValue().getLakkautusPvm();

            if (endDate == null) {
                if (parentEndDate == null) {
                    // Both null, ok
                    return true;
                }

                // XXXX side-effect:
                // Copy parent end date
                if (!parentEndDate.after(new Date())) {
                    parentChild.getValue().setLakkautusPvm(parentEndDate);
                }
                return true;
            } else {
                // End date not null

                // Parent end date null?
                if (parentEndDate == null) {
                    return true;
                }

                // endDate <= parentEndDate
                return !endDate.after(parentEndDate);
            }
        }
    };

    private boolean isSkipParentDateValidation;

    public OrganisationDateValidator(boolean skipParentDateValidation) {
        this.isSkipParentDateValidation = skipParentDateValidation;
    }

    @Override
    public boolean apply(@Nullable Entry<Organisaatio, Organisaatio> parentChild) {
        if(isSkipParentDateValidation) {
            return Predicates.and(endDateValidator, dateValidator).apply(parentChild);
        } else {
            return Predicates.and(startDateValidator, endDateValidator, dateValidator).apply(parentChild);
        }
    }
}
