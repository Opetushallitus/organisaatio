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
package fi.vm.sade.organisaatio.service.converter;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import fi.vm.sade.organisaatio.api.model.types.SoMeLinkkiTyyppi;
import fi.vm.sade.organisaatio.api.model.types.SoMeLinkkiTyyppiTyyppi;
import fi.vm.sade.organisaatio.model.OrganisaatioMetaData;

public class EntityToSoMeLinkitFunctionTest {

    @Test
    public void test() {
        EntityToSoMeLinkitFunction f = new EntityToSoMeLinkitFunction();

        //null metadata
        List<SoMeLinkkiTyyppi> linkit = f.apply(null);
        Assert.assertEquals(0, linkit.size());

        //empty metadata
        OrganisaatioMetaData md = new OrganisaatioMetaData();
        linkit = f.apply(md);
        Assert.assertEquals(0, linkit.size());

        //one link
        md.setNamedValue(SoMeLinkkiTyyppiTyyppi.FACEBOOK.toString(), "", "http://www.fi");
        linkit = f.apply(md);
        Assert.assertEquals(1, linkit.size());
        Assert.assertEquals(SoMeLinkkiTyyppiTyyppi.FACEBOOK ,linkit.get(0).getTyyppi());
        Assert.assertEquals("http://www.fi" ,linkit.get(0).getSisalto());

        //two links
        md.setNamedValue(SoMeLinkkiTyyppiTyyppi.GOOGLE_PLUS.toString(), "", "http://www.fi");
        linkit = f.apply(md);
        Assert.assertEquals(2, linkit.size());

        //two of type muu
        md.setNamedValue(SoMeLinkkiTyyppiTyyppi.MUU.toString(), "1", "http://www.fi");
        md.setNamedValue(SoMeLinkkiTyyppiTyyppi.MUU.toString(), "2", "http://www.fi");
        linkit = f.apply(md);
        Assert.assertEquals(4, linkit.size());
}

}
