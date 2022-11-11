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

import fi.vm.sade.organisaatio.service.util.OrganisaatioUtil;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.GregorianCalendar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class OrganisaatioServiceAlgorithmsTest {
	
	
	private static Date testDate(int n) {
		return new GregorianCalendar(2000+n, 1,1).getTime();
	}

	@Test
	public void testLakkautusPvmLogic() {
		
		Date D1 = testDate(1);
		Date D2 = testDate(2);
		Date D3 = testDate(3);
		
		// ei ylempää lpvm:ää hierarkiassa
		assertNull(OrganisaatioUtil.getUpdatedLakkautusPvm(null, null, null, null));
		assertEquals(D1, OrganisaatioUtil.getUpdatedLakkautusPvm(null, D1, null, null));
		assertEquals(D1, OrganisaatioUtil.getUpdatedLakkautusPvm(D2, D1, null, null));
		assertNull(OrganisaatioUtil.getUpdatedLakkautusPvm(D2, null, null, null));

		// parentilla lpvm
		assertEquals(D1, OrganisaatioUtil.getUpdatedLakkautusPvm(D1, D2, null, D1));
		assertEquals(D2, OrganisaatioUtil.getUpdatedLakkautusPvm(D1, D2, null, D2));
		
		// myöhennys
		assertEquals(D1, OrganisaatioUtil.getUpdatedLakkautusPvm(D1, D2, D1, D1));
		assertEquals(D2, OrganisaatioUtil.getUpdatedLakkautusPvm(D1, D2, D1, D2));

		assertEquals(D3, OrganisaatioUtil.getUpdatedLakkautusPvm(D1, D3, D1, D3));
		assertEquals(D2, OrganisaatioUtil.getUpdatedLakkautusPvm(D2, D3, D1, D3));

		// aiennus
		assertEquals(D1, OrganisaatioUtil.getUpdatedLakkautusPvm(D2, D1, D2, D2));
		assertEquals(D1, OrganisaatioUtil.getUpdatedLakkautusPvm(D2, D1, D2, D1));
		assertEquals(D2, OrganisaatioUtil.getUpdatedLakkautusPvm(D2, D1, D3, D1));
		assertEquals(D1, OrganisaatioUtil.getUpdatedLakkautusPvm(D3, D1, D3, D1));
		
		// poisto
		assertNull(OrganisaatioUtil.getUpdatedLakkautusPvm(D2, null, D2, null));
		assertEquals(D2, OrganisaatioUtil.getUpdatedLakkautusPvm(D2, null, D2, D2));
		assertEquals(D1, OrganisaatioUtil.getUpdatedLakkautusPvm(D1, null, D2, D2));
		assertEquals(D1, OrganisaatioUtil.getUpdatedLakkautusPvm(D1, null, D2, null));

		// OVT-3748
		assertEquals(D2, OrganisaatioUtil.getUpdatedLakkautusPvm(D2, D3, D2, D2));

		
	}
	
}
