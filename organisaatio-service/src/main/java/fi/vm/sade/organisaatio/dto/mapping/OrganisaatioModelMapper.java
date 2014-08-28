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

import fi.vm.sade.organisaatio.dto.v2.OrganisaatioNimiDTOV2;
import fi.vm.sade.organisaatio.dto.v2.OrganisaatioYhteystiedotDTOV2;
import fi.vm.sade.organisaatio.dto.v2.OsoiteDTOV2;
import fi.vm.sade.organisaatio.model.Email;
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.model.OrganisaatioNimi;
import fi.vm.sade.organisaatio.model.Osoite;
import fi.vm.sade.organisaatio.model.Puhelinnumero;
import fi.vm.sade.organisaatio.model.Www;
import fi.vm.sade.organisaatio.model.Yhteystieto;
import fi.vm.sade.organisaatio.service.util.YhteystietoUtil;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.TypeToken;
import org.modelmapper.spi.MappingContext;


/**
 *
 * @author simok
 */
public class OrganisaatioModelMapper extends ModelMapper {

    public OrganisaatioModelMapper() {
        super();

        // PostiOsoiteConverter
        final Converter<List<Yhteystieto>, List<OsoiteDTOV2>> postiOsoiteConverter = new Converter<List<Yhteystieto>, List<OsoiteDTOV2>>() {
            @Override
            public List<OsoiteDTOV2> convert(MappingContext<List<Yhteystieto>, List<OsoiteDTOV2>> mc) {
                OsoiteModelMapper modelMapper = new OsoiteModelMapper();

                // Define the target list type for mapping
                Type osoiteDTOV2ListType = new TypeToken<List<OsoiteDTOV2>>() {}.getType();

                List<Osoite> postiOsoitteet = YhteystietoUtil.getPostiOsoitteet(mc.getSource());

                // Map domain type to DTO
                return modelMapper.map(postiOsoitteet, osoiteDTOV2ListType);
            }
        };

        // KayntiOsoiteConverter
        final Converter<List<Yhteystieto>, List<OsoiteDTOV2>> kayntiOsoiteConverter = new Converter<List<Yhteystieto>, List<OsoiteDTOV2>>() {
            @Override
            public List<OsoiteDTOV2> convert(MappingContext<List<Yhteystieto>, List<OsoiteDTOV2>> mc) {
                OsoiteModelMapper modelMapper = new OsoiteModelMapper();

                // Define the target list type for mapping
                Type osoiteDTOV2ListType = new TypeToken<List<OsoiteDTOV2>>() {}.getType();

                List<Osoite> postiOsoitteet = YhteystietoUtil.getKayntiOsoitteet(mc.getSource());

                // Map domain type to DTO
                return modelMapper.map(postiOsoitteet, osoiteDTOV2ListType);
            }
        };

        // wwwOsoiteConverter
        final Converter<List<Yhteystieto>, Map<String, String>> wwwOsoiteConverter = new Converter<List<Yhteystieto>, Map<String, String>>() {
            @Override
            public Map<String, String> convert(MappingContext<List<Yhteystieto>, Map<String, String>> mc) {
                List<Www> wwwOsoitteet = YhteystietoUtil.getWwwOsoitteet(mc.getSource());

                // Tehdään map, jossa avaimena kieli ja arvone www osoite
                Map<String, String> wwwOsoiteMap = new HashMap<String, String>();

                for (Www www : wwwOsoitteet) {
                    wwwOsoiteMap.put(www.getKieli(), www.getWwwOsoite());
                }

                return wwwOsoiteMap;
            }
        };

        // wwwOsoiteConverter
        final Converter<List<Yhteystieto>, Map<String, String>> emailOsoiteConverter = new Converter<List<Yhteystieto>, Map<String, String>>() {
            @Override
            public Map<String, String> convert(MappingContext<List<Yhteystieto>, Map<String, String>> mc) {
                List<Email> emailOsoitteet = YhteystietoUtil.getEmailOsoitteet(mc.getSource());

                // Tehdään map, jossa avaimena kieli ja arvona email osoite
                Map<String, String> emailOsoiteMap = new HashMap<String, String>();

                for (Email email : emailOsoitteet) {
                    emailOsoiteMap.put(email.getKieli(), email.getEmail());
                }

                return emailOsoiteMap;
            }
        };

        // puhelinnumeroConverter
        final Converter<List<Yhteystieto>, Map<String, String>> puhelinnumeroConverter = new Converter<List<Yhteystieto>, Map<String, String>>() {
            @Override
            public Map<String, String> convert(MappingContext<List<Yhteystieto>, Map<String, String>> mc) {
                List<Puhelinnumero> puhelinnumerot = YhteystietoUtil.getPuhelinnumerot(mc.getSource());

                // Tehdään map, jossa avaimena kieli ja arvone puhelinnumero
                Map<String, String> puhelinnumeroMap = new HashMap<String, String>();

                for (Puhelinnumero numero : puhelinnumerot) {
                    puhelinnumeroMap.put(numero.getKieli(), numero.getPuhelinnumero());
                }

                return puhelinnumeroMap;
            }
        };

        // faksinumeroConverter
        final Converter<List<Yhteystieto>, Map<String, String>> faksinumeroConverter = new Converter<List<Yhteystieto>, Map<String, String>>() {
            @Override
            public Map<String, String> convert(MappingContext<List<Yhteystieto>, Map<String, String>> mc) {
                List<Puhelinnumero> faksinumerot = YhteystietoUtil.getFaksinumerot(mc.getSource());

                // Tehdään map, jossa avaimena kieli ja arvone faksinumero
                Map<String, String> faksinumeroMap = new HashMap<String, String>();

                for (Puhelinnumero numero : faksinumerot) {
                    faksinumeroMap.put(numero.getKieli(), numero.getPuhelinnumero());
                }

                return faksinumeroMap;
            }
        };

        this.addMappings(new PropertyMap<Organisaatio, OrganisaatioYhteystiedotDTOV2>() {
            @Override
            protected void configure() {
                // Monikielinen nimi
                map().setNimi(source.getNimi().getValues());

                // Postiosoite
                using(postiOsoiteConverter).map(source.getYhteystiedot()).setPostiosoite(null);

                // Käyntiosoite
                using(kayntiOsoiteConverter).map(source.getYhteystiedot()).setKayntiosoite(null);

                // Puhelinnumero
                using(puhelinnumeroConverter).map(source.getYhteystiedot()).setPuhelinnumero(null);

                // Puhelinnumero
                using(faksinumeroConverter).map(source.getYhteystiedot()).setFaksinumero(null);

                // WWW-osoite
                using(wwwOsoiteConverter).map(source.getYhteystiedot()).setWwwOsoite(null);

                // Email-osoite
                using(emailOsoiteConverter).map(source.getYhteystiedot()).setEmailOsoite(null);

            }
        });
    }
}
