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
package fi.vm.sade.organisaatio.service.converter.v3;

import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.dto.mapping.OrganisaatioNimiModelMapper;
import fi.vm.sade.organisaatio.dto.v3.OrganisaatioRDTOV3;
import fi.vm.sade.organisaatio.model.*;
import fi.vm.sade.organisaatio.resource.dto.OrganisaatioNimiRDTO;
import fi.vm.sade.organisaatio.service.converter.util.MetadataConverterUtils;
import fi.vm.sade.organisaatio.service.converter.util.YhteystietoConverterUtils;
import org.modelmapper.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;
import static fi.vm.sade.organisaatio.service.util.DateUtil.toTimestamp;

@Component
public class OrganisaatioToOrganisaatioRDTOV3Converter implements Converter<Organisaatio, OrganisaatioRDTOV3> {

    private static final Logger LOG = LoggerFactory.getLogger(OrganisaatioToOrganisaatioRDTOV3Converter.class);

    private final OrganisaatioNimiModelMapper organisaatioNimiModelMapper;
    private final Type organisaatioNimiRDTOListType;

    public OrganisaatioToOrganisaatioRDTOV3Converter(OrganisaatioNimiModelMapper organisaatioNimiModelMapper) {
        this.organisaatioNimiRDTOListType = new TypeToken<List<OrganisaatioNimiRDTO>>() {}.getType();
        this.organisaatioNimiModelMapper = organisaatioNimiModelMapper;
    }

    @Override
    public OrganisaatioRDTOV3 convert(Organisaatio s) {
        long qstarted = System.currentTimeMillis();

        OrganisaatioRDTOV3 t = new OrganisaatioRDTOV3();

        t.setOid(s.getOid());
        t.setVersion(s.getVersion() != null ? s.getVersion().intValue() : 0);

        t.setAlkuPvm(s.getAlkuPvm());
        t.setDomainNimi(s.getDomainNimi());

        t.setKieletUris(convertCollectionToSet(s.getKielet()));
        t.setKotipaikkaUri(s.getKotipaikka());
        t.setKuvaus2(convertMKTToMap(s.getKuvaus2()));
        t.setLakkautusPvm(s.getLakkautusPvm());
        t.setMaaUri(s.getMaa());
        t.setMetadata(MetadataConverterUtils.convertMetadata(s.getMetadata()));

        t.setStatus(s.getStatus().name());

        t.setOppilaitosKoodi(s.getOppilaitosKoodi());
        t.setOppilaitosTyyppiUri(s.getOppilaitosTyyppi());
        t.setParentOid(s.getParent() != null ? s.getParent().getOid() : null);
        t.setParentOidPath(s.getParentOidPath());

        t.setOpetuspisteenJarjNro(s.getOpetuspisteenJarjNro());
        t.setToimipistekoodi(s.getToimipisteKoodi());
        t.setTyypit(OrganisaatioTyyppi.tyypitFromKoodis(s.getTyypit()));
        t.setLisatiedot(convertSetToSet(s.getOrganisaatioLisatietotyypit().stream()
                .map(OrganisaatioLisatietotyyppi::getLisatietotyyppi)
                .map(Lisatietotyyppi::getNimi)
                .collect(Collectors.toSet())));
        t.setVuosiluokat(convertCollectionToSet(s.getVuosiluokat()));
        t.setRyhmatyypit(convertSetToSet(s.getRyhmatyypit()));
        t.setKayttoryhmat(convertSetToSet(s.getKayttoryhmat()));
        t.setYhteishaunKoulukoodi(s.getYhteishaunKoulukoodi());
        t.setYritysmuoto(s.getYritysmuoto());
        t.setYTJKieli(s.getYtjKieli());
        t.setYTJPaivitysPvm(s.getYtjPaivitysPvm());
        t.setYTunnus(s.getYtunnus());
        t.setVirastoTunnus(s.getVirastoTunnus());
        t.setTarkastusPvm(toTimestamp(s.getTarkastusPvm()));

        // Get dynamic Yhteysieto / Yhteystietotyppie / Elementti data
        Set<Map<String, String>> yhteystietoArvos = new HashSet<>();
        t.setYhteystietoArvos(yhteystietoArvos);

        t.setNimi(convertMKTToMap(s.getNimi()));
        t.setNimet(organisaatioNimiModelMapper.map(s.getNimet(), organisaatioNimiRDTOListType));
        t.setKayntiosoite(YhteystietoConverterUtils.convertOsoiteToMap(s.getKayntiosoite()));
        t.setPostiosoite(YhteystietoConverterUtils.convertOsoiteToMap(s.getPostiosoite()));

        for (Yhteystieto y : s.getYhteystiedot()) {
            t.addYhteystieto(YhteystietoConverterUtils.mapYhteystietoToGeneric(y));
        }
        YhteystietoConverterUtils.convertYhteystietosToListMap(s, yhteystietoArvos);

        LOG.debug("convert: {} --> " + t.getClass().getSimpleName() + " in {} ms", s, System.currentTimeMillis() - qstarted);

        return t;
    }

    private Map<String, String> convertMKTToMap(MonikielinenTeksti nimi) {
        Map<String, String> result = new HashMap<>();

        if (nimi != null) {
            // Lisätään vastauksiin kaikki nimen kielet, joissa tekstiä
            for (Map.Entry<String, String> entry : nimi.getValues().entrySet()) {
                if (isNullOrEmpty(entry.getValue()) == false) {
                    result.put(entry.getKey(), entry.getValue());
                }
            }
        }
        return result;
    }

    private Set<String> convertCollectionToSet(Collection<String> s) {
        return new HashSet<>(s);
    }

    private Set<String> convertSetToSet(Collection<String> s) {
        return new HashSet<>(s);
    }

}
