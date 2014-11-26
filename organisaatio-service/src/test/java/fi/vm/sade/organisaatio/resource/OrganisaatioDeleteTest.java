/*
* Copyright (c) 2014 The Finnish Board of Education - Opetushallitus
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
*/

package fi.vm.sade.organisaatio.resource;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Joiner;

import fi.vm.sade.organisaatio.SecurityAwareTestBase;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.resource.dto.OrganisaatioRDTO;
import fi.vm.sade.organisaatio.util.OrganisaatioRDTOTestUtil;

@ContextConfiguration(locations = { "classpath:spring/test-context.xml" })
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@ActiveProfiles("embedded-solr")
public class OrganisaatioDeleteTest extends SecurityAwareTestBase {

    @Autowired
    OrganisaatioResource res;

    private static final Logger LOG = LoggerFactory.getLogger(OrganisaatioDeleteTest.class);

    @Value("${root.organisaatio.oid}")
    private String rootOrganisaatioOid;

    @Test
    public void testDelete() throws Exception {
        LOG.info("doTest()...");
        OrganisaatioRDTO a = createOrganisaatio("A", null);
        OrganisaatioRDTO b = createOrganisaatio("B", a);
        OrganisaatioRDTO c = createOrganisaatio("C", b);
        OrganisaatioRDTO d = createOrganisaatio("D", c);
        OrganisaatioRDTO e = createOrganisaatio("E", d);

        String reference = Joiner.on("/").join(
                new String[] { rootOrganisaatioOid, a.getOid(), b.getOid(),
                        c.getOid(), d.getOid(), e.getOid() });

        String s = res.parentoids(e.getOid());
        Assert.assertEquals(reference, s);
    }

    private OrganisaatioRDTO createOrganisaatio(String nimi, OrganisaatioRDTO parent) {
        LOG.info("createOrganisaatio({})", nimi);

        OrganisaatioRDTO o = OrganisaatioRDTOTestUtil.createOrganisaatio(nimi, OrganisaatioTyyppi.MUU_ORGANISAATIO.value(), parent);

        return res.newOrganisaatio(o).getOrganisaatio();
    }

}
