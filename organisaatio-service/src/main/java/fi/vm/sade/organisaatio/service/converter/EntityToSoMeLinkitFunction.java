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

import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import fi.vm.sade.organisaatio.api.model.types.SoMeLinkkiTyyppi;
import fi.vm.sade.organisaatio.api.model.types.SoMeLinkkiTyyppiTyyppi;
import fi.vm.sade.organisaatio.model.MonikielinenTeksti;
import fi.vm.sade.organisaatio.model.OrganisaatioMetaData;

public class EntityToSoMeLinkitFunction implements Function<OrganisaatioMetaData, List<SoMeLinkkiTyyppi>> {

    @Override
    public List<SoMeLinkkiTyyppi> apply(@Nullable OrganisaatioMetaData metadata) {
        final List<SoMeLinkkiTyyppi> linkit = Lists.newArrayList();
        if (metadata == null) {
            return linkit;
        }
        for (SoMeLinkkiTyyppiTyyppi tyyppi : SoMeLinkkiTyyppiTyyppi.values()) {
            final MonikielinenTeksti value = metadata.getNamedValue(tyyppi.toString());
            if (value != null) {
                for(String someValue: value.getValues().values()){
                    final SoMeLinkkiTyyppi someLinkki = new SoMeLinkkiTyyppi();
                    someLinkki.setTyyppi(tyyppi);
                    someLinkki.setSisalto(someValue);
                    linkit.add(someLinkki);
                }
            }
        }
        return linkit;
    }
}
