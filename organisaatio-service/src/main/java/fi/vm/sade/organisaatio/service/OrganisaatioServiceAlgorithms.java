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

import java.util.Date;

import com.google.common.base.Objects;

/**
 * Organisaatiologiikka-algoritmejä (staattisessa luokassa unit-testauksen helpottamiseksi).
 * 
 * @author Timo Santasalo / Teknokala Ky
 */
public final class OrganisaatioServiceAlgorithms {

	private OrganisaatioServiceAlgorithms() {}
	
    /**
     * Organisaation lakkautuspvm -logiikka. Huom. kaikki parametrit voivat olla null.
     * 
     * @param oldLpvm Päivitettävän organisaation nykyinen lakkautuspvm.
     * @param newLpvm Uusi lakkautuspvm.
     * @param origLpvm Päivitettävän organisaatiojoukun alkuperäinen lakkautuspvm.
     * @param parentLpvm Ylemmän tason organisaation lakkautuspvm.
     * @return
     */
    public static Date getUpdatedLakkautusPvm(Date oldLpvm, Date newLpvm, Date origLpvm, Date parentLpvm) {

    	if (parentLpvm != null && (newLpvm == null || newLpvm.after(parentLpvm))) {
    		newLpvm = parentLpvm;
    	}
    	
    	if (origLpvm != null && !Objects.equal(oldLpvm, origLpvm)) {
    		return oldLpvm;
    	} else {
    		return newLpvm;
    	}
    }

}
