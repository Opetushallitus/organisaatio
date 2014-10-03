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

import javax.annotation.Nullable;

import com.google.common.base.Function;

import fi.vm.sade.organisaatio.api.model.types.HakutoimistoTyyppi;
import fi.vm.sade.organisaatio.api.model.types.YhteyshenkiloTyyppi;
import fi.vm.sade.organisaatio.api.model.types.YhteystietoDTO;
import fi.vm.sade.organisaatio.model.lop.OrganisaatioMetaData;

public class EntityToHakutoimistoFunction implements Function<OrganisaatioMetaData, HakutoimistoTyyppi> {

    private EntityToMonikielinenTekstiTyyppiFunction toMonikielinenTekstiTyyppiFunction = new EntityToMonikielinenTekstiTyyppiFunction();

    private final ConverterFactory converterFactory;

    public EntityToHakutoimistoFunction(ConverterFactory converterFactory) {
        this.converterFactory = converterFactory;
    }

    @Override
    public HakutoimistoTyyppi apply(@Nullable OrganisaatioMetaData metadata) {
        HakutoimistoTyyppi dto = new HakutoimistoTyyppi();
        if (metadata == null) {
            return dto;
        }
        dto.setOpintotoimistoNimi(toMonikielinenTekstiTyyppiFunction.apply(metadata.getHakutoimistoNimi()));

        YhteyshenkiloTyyppi yhteyshenkilo = new YhteyshenkiloTyyppi();
        yhteyshenkilo.setEmail(metadata.getHakutoimistoEctsEmail_old());
        yhteyshenkilo.setKokoNimi(metadata.getHakutoimistoEctsNimi_old());
        yhteyshenkilo.setPuhelin(metadata.getHakutoimistoEctsPuhelin_old());
        yhteyshenkilo.setTitteli(metadata.getHakutoimistoEctsTehtavanimike_old());
        dto.setEctsYhteyshenkilo(yhteyshenkilo);

        dto.getOpintotoimistoYhteystiedot().addAll(
                converterFactory.convertToDTO(metadata.getYhteystiedot(), YhteystietoDTO.class));
        return dto;
    }
}
