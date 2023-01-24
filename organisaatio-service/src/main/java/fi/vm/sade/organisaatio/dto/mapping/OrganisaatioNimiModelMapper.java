/*
* Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
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

package fi.vm.sade.organisaatio.dto.mapping;

import fi.vm.sade.organisaatio.dto.OrganisaatioNimiDTO;
import fi.vm.sade.organisaatio.model.MonikielinenTeksti;
import fi.vm.sade.organisaatio.model.OrganisaatioNimi;
import fi.vm.sade.organisaatio.resource.dto.OrganisaatioNimiRDTO;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.spi.MappingContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Strings.isNullOrEmpty;

@Component
public class OrganisaatioNimiModelMapper extends ModelMapper {

    public OrganisaatioNimiModelMapper() {
        super();

        // Monikielinen teksti
        final Converter<Map<String, String>, MonikielinenTeksti> monikielinentTekstiConverter =
                new Converter<Map<String, String>, MonikielinenTeksti>() {
            @Override
            public MonikielinenTeksti convert(MappingContext<Map<String, String>, MonikielinenTeksti> mc) {
                MonikielinenTeksti mt = new MonikielinenTeksti();
                for (Map.Entry<String, String> e : mc.getSource().entrySet()) {
                    mt.addString(e.getKey(), e.getValue());
                }
                return mt;
            }
        };

        // Poistetaan tyhjät stringit
        final Converter<Map<String, String>, Map<String, String>> mapRemoveEmptyValuesConverter =
                new Converter<Map<String, String>, Map<String, String>>() {
            @Override
            public Map<String, String> convert(MappingContext<Map<String, String>, Map<String, String>> mc) {
                Map<String, String> result = new HashMap<>();
                for (Map.Entry<String, String> entry : mc.getSource().entrySet()) {
                    if (isNullOrEmpty(entry.getValue()) == false) {
                        result.put(entry.getKey(), entry.getValue());
                    }
                }

                return result;
            }
        };

        this.addMappings(new PropertyMap<OrganisaatioNimi, OrganisaatioNimiDTO>() {
            @Override
            protected void configure() {
                // Monikielinen nimi --> Map<String, String>
                // Lisätään kaikki nimen kieliversiot, jotka eivät ole tyhjiä
                using(mapRemoveEmptyValuesConverter).map(source.getNimi().getValues()).setNimi(null);
                map().setOid(source.getOrganisaatio().getOid());
            }
        });

        this.addMappings(new PropertyMap<OrganisaatioNimiDTO, OrganisaatioNimi>() {
            @Override
            protected void configure() {
                // Monikielinen teksti -- nimi
                using(monikielinentTekstiConverter).map(source.getNimi()).setNimi(null);
            }
        });

        this.addMappings(new PropertyMap<OrganisaatioNimi, OrganisaatioNimiRDTO>() {
            @Override
            protected void configure() {
                // Monikielinen nimi --> Map<String, String>
                // Lisätään kaikki nimen kieliversiot, jotka eivät ole tyhjiä
                using(mapRemoveEmptyValuesConverter).map(source.getNimi().getValues()).setNimi(null);
            }
        });

        this.addMappings(new PropertyMap<OrganisaatioNimiRDTO, OrganisaatioNimi>() {
            @Override
            protected void configure() {
                // Monikielinen teksti -- nimi
                using(monikielinentTekstiConverter).map(source.getNimi()).setNimi(null);
            }
        });
    }
}
