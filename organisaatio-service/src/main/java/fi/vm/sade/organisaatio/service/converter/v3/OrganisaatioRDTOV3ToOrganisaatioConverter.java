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
package fi.vm.sade.organisaatio.service.converter.v3;

import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.dto.mapping.OrganisaatioNimiModelMapper;
import fi.vm.sade.organisaatio.dto.v3.OrganisaatioRDTOV3;
import fi.vm.sade.organisaatio.model.*;
import fi.vm.sade.organisaatio.service.converter.util.MetadataConverterUtils;
import fi.vm.sade.organisaatio.service.converter.util.MonikielinenTekstiConverterUtils;
import fi.vm.sade.organisaatio.service.converter.util.YhteystietoConverterUtils;
import fi.vm.sade.organisaatio.service.util.OrganisaatioNimiUtil;
import fi.vm.sade.organisaatio.service.util.OrganisaatioUtil;
import org.modelmapper.TypeToken;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class OrganisaatioRDTOV3ToOrganisaatioConverter implements Converter<OrganisaatioRDTOV3, Organisaatio> {

    private final OrganisaatioNimiModelMapper organisaatioNimiModelMapper;

    public OrganisaatioRDTOV3ToOrganisaatioConverter(OrganisaatioNimiModelMapper organisaatioNimiModelMapper) {
        this.organisaatioNimiModelMapper = organisaatioNimiModelMapper;
    }

    @Override
    public Organisaatio convert(OrganisaatioRDTOV3 t) {
        Set<Yhteystieto> yhteystietos = new HashSet<>();
        Organisaatio s = new Organisaatio();

        s.setOid(t.getOid());
        s.setVersion((long)t.getVersion());

        s.setAlkuPvm(t.getAlkuPvm());
        // t.setChildCount(s.getChildCount());
        s.setDomainNimi(t.getDomainNimi());

        s.setKielet(convertCollectionToSet(t.getKieletUris()));
        s.setKotipaikka(t.getKotipaikkaUri());
        s.setKuvaus2(MonikielinenTekstiConverterUtils.convertMapToMonikielinenTeksti(t.getKuvaus2()));
        s.setLakkautusPvm(t.getLakkautusPvm());
        s.setMaa(t.getMaaUri());
        s.setMetadata(MetadataConverterUtils.convertMetadata(t.getMetadata()));
        s.setNimi(MonikielinenTekstiConverterUtils.convertMapToMonikielinenTeksti(t.getNimi()));

        // Define the target list type for mapping
        Type organisaatioNimiListType = new TypeToken<List<OrganisaatioNimi>>() {}.getType();

        // Map DTO to domain type
        s.setNimet(organisaatioNimiModelMapper.map(t.getNimet(), organisaatioNimiListType));

        // Asetetaan nimihakuun nimeksi nimihistorian current nimi, tai uusin nimi
        MonikielinenTeksti nimi = OrganisaatioNimiUtil.getNimi(s.getNimet());
        s.setNimihaku(OrganisaatioNimiUtil.createNimihaku(nimi));

        // t.set(s.getNimiLyhenne());
        s.setOpetuspisteenJarjNro(t.getOpetuspisteenJarjNro());
        s.setOppilaitosKoodi(t.getOppilaitosKoodi());
        s.setOppilaitosTyyppi(t.getOppilaitosTyyppiUri());
        // s.setParentOid(s.getParent() != null ? s.getParent().getOid() : null);
        // t.set(s.getParentIdPath());
        // t.setParentMetadata(s.getParentMetadata());
        s.setParentOids(OrganisaatioUtil.parentOids(s.getParentOidPath()));
        // t.set(s.getParentSuhteet());

        // t.set(s.getPuhelin());
        s.setToimipisteKoodi(t.getToimipistekoodi());
        s.setTyypit(OrganisaatioTyyppi.tyypitToKoodis(t.getTyypit()));
        // t.set(s.getTyypitAsString());
        s.setVuosiluokat(convertCollectionToSet(t.getVuosiluokat()));
        s.setOrganisaatioLisatietotyypit(t.getLisatiedot().stream()
                .map(lisatietoNimi -> {
                    OrganisaatioLisatietotyyppi organisaatioLisatietotyyppi = new OrganisaatioLisatietotyyppi();
                    Lisatietotyyppi lisatietotyyppi = new Lisatietotyyppi();
                    lisatietotyyppi.setNimi(lisatietoNimi);
                    organisaatioLisatietotyyppi.setLisatietotyyppi(lisatietotyyppi);
                    organisaatioLisatietotyyppi.setOrganisaatio(s);
                    return organisaatioLisatietotyyppi;
                })
                .collect(Collectors.toSet()));
        s.setRyhmatyypit(convertSetToSet(t.getRyhmatyypit()));
        s.setKayttoryhmat(convertSetToSet(t.getKayttoryhmat()));
        s.setYhteishaunKoulukoodi(t.getYhteishaunKoulukoodi());
        // t.set(s.getYhteystiedot());
        // t.set(s.getYhteystietoArvos());
        s.setYritysmuoto(t.getYritysmuoto());
        s.setYtjKieli(t.getYTJKieli());
        s.setYtjPaivitysPvm(t.getYTJPaivitysPvm());
        s.setYtunnus(t.getYTunnus());
        s.setVirastoTunnus(t.getVirastoTunnus());
        s.setTarkastusPvm(t.getTarkastusPvm());

        if (t.getYhteystietoArvos()!=null) {
            s.setYhteystietoArvos(YhteystietoConverterUtils.convertYhteystietoArvos(t.getYhteystietoArvos()));
        }

        for (Map<String, String> m : t.getYhteystiedot()) {
            Yhteystieto y = YhteystietoConverterUtils.convertYhteystietoGeneric(m);
            if (y != null) {
                yhteystietos.add(y);
            }
        }
        s.setYhteystiedot(yhteystietos);

        return s;
    }

    private Set<String> convertCollectionToSet(Collection<String> s) {
        return new HashSet<>(s);
    }

    private Set<String> convertSetToSet(Set<String> s) {
        return new HashSet<>(s);
    }
}
