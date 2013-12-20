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
package fi.vm.sade.tarjoaja.service;

import java.util.List;
import java.util.Map.Entry;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import fi.vm.sade.organisaatio.model.MonikielinenTeksti;
import fi.vm.sade.organisaatio.model.lop.NamedMonikielinenTeksti;
import fi.vm.sade.organisaatio.model.lop.OrganisaatioMetaData;
import fi.vm.sade.tarjoaja.service.types.FindByOrganizationOidRequestType;
import fi.vm.sade.tarjoaja.service.types.FindByOrganizationOidResponseType;
import fi.vm.sade.tarjoaja.service.types.KielistettyTekstiTyyppi;
import fi.vm.sade.tarjoaja.service.types.KoulutustarjoajaTyyppi;
import fi.vm.sade.tarjoaja.service.types.MetatietoArvoTyyppi;
import fi.vm.sade.tarjoaja.service.types.MetatietoAvainTyyppi;
import fi.vm.sade.tarjoaja.service.types.MetatietoTyyppi;

/**
 * Services for KoulutusTarjoaja (which is Organisation).
 *
 * @author mlyly
 */
@Transactional(rollbackFor = {GenericFault.class})
public class KoulutustarjoajaPublicServiceImpl implements KoulutustarjoajaPublicService {

    private static final Logger LOG = LoggerFactory.getLogger(KoulutustarjoajaPublicServiceImpl.class);
    @PersistenceContext
    private EntityManager em;

    @Override
    public FindByOrganizationOidResponseType findByOrganizationOid(FindByOrganizationOidRequestType parameters) throws GenericFault {
        LOG.info("findByOrganizationOid({})", parameters.getOid());


        // Create result holder
        FindByOrganizationOidResponseType result = new FindByOrganizationOidResponseType();

        // Find LOP and convert
        OrganisaatioMetaData lop = findByOrganizationOid(parameters.getOid());
        if (lop == null) {
            result.setReturn(null);
        } else {
            KoulutustarjoajaTyyppi dto = convertToDTO(lop);
            result.setReturn(dto);
        }

        LOG.info("  findByOrganizationOid({}) result => {}", parameters.getOid(), result.getReturn());

        return result;
    }

    /**
     * Find by organization OID. There can be only one.
     *
     * @param organisationOid
     * @return
     */
    private OrganisaatioMetaData findByOrganizationOid(String organisationOid) {
        return em.createQuery("SELECT m FROM OrganisaatioMetaData m WHERE m.organisation.oid = :oid", OrganisaatioMetaData.class)
                .setParameter("oid", organisationOid).getSingleResult();
    }

    /**
     * Convert to DTO.
     *
     * @param lop
     * @return
     */
    private KoulutustarjoajaTyyppi convertToDTO(OrganisaatioMetaData lop) {
        LOG.info("convertToDTO(): lop={}", lop);

        KoulutustarjoajaTyyppi result = new KoulutustarjoajaTyyppi();

        // TODO copy organisatio information also!
        // - oid
        // - nimi?

        result.setLuontiPvm(lop.getLuontiPvm());
        result.setMuokkausPvm(lop.getMuokkausPvm());
        // result.setOid(lop.getOid());
        result.setOrganisaatioOid(lop.getOrganisation().getOid());
        result.setVersion(lop.getVersion());

        copyFields(lop.getOrganisation().getNimi(), result.getNimi());


        // Copy translataed textual values to DTO
        for (NamedMonikielinenTeksti namedMonikielinenTeksti : lop.getValues()) {
            // Keyed translated content value (key: for example: "GenericInformationAboutStudies")
            MetatietoTyyppi meta = new MetatietoTyyppi();
            result.getMetatieto().add(meta);
            LOG.info("convertToDTO(): MetatietoAvainTyyppi={} ", namedMonikielinenTeksti.getKey());
            MetatietoAvainTyyppi sisaltoTyyppi = MetatietoAvainTyyppi.valueOf(namedMonikielinenTeksti.getKey());
            if (sisaltoTyyppi == null) {
                // Map any unkown types to UNKNOWN
                sisaltoTyyppi = MetatietoAvainTyyppi.UNKNOWN;
                LOG.warn("LearningOpportunityProvider --> LearningOpportunityProviderDTO: convertToDTO: Mapping type: {} to {}",
                        namedMonikielinenTeksti.getKey(), sisaltoTyyppi);
            }

            // Set data "key"
            meta.setAvain(sisaltoTyyppi);

            // Add data translations in all languages
            for (String kieliKoodi : namedMonikielinenTeksti.getValue().getValues().keySet()) {
                MetatietoArvoTyyppi metaArvo = new MetatietoArvoTyyppi();
                metaArvo.setKieliKoodi(kieliKoodi);
                metaArvo.setArvo(namedMonikielinenTeksti.getValue().getString(kieliKoodi));

                meta.getArvos().add(metaArvo);
            }
        }

        LOG.info("convertToDTO(): lop={} --> result={}", lop, result);

        return result;
    }

    public OrganisaatioMetaData findById(Long id) {
        OrganisaatioMetaData lop =
                em.createQuery("SELECT p FROM OrganisaatioMetaData p WHERE id = :id", OrganisaatioMetaData.class).setParameter("id", id).getSingleResult();
        return lop;
    }

    private void copyFields(MonikielinenTeksti from, List<KielistettyTekstiTyyppi> to) {

        for (Entry<String, String> kv : from.getValues().entrySet()) {
            KielistettyTekstiTyyppi toName = new KielistettyTekstiTyyppi();
            toName.setLang(kv.getKey());
            toName.setValue(kv.getValue());
            to.add(toName);
        }

    }
}
