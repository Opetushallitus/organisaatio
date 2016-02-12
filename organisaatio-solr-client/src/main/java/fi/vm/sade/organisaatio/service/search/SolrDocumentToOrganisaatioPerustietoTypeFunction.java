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
package fi.vm.sade.organisaatio.service.search;

import java.util.Collection;
import java.util.Date;
import java.util.Set;

import org.apache.solr.common.SolrDocument;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;

import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.api.search.OrganisaatioPerustieto;

/**
 * Convert {@link SolrDocument} from solr to {@link OrganisaatioPerustieto}
 */
public class SolrDocumentToOrganisaatioPerustietoTypeFunction extends
        SolrOrgFields implements Function<SolrDocument, OrganisaatioPerustieto> {

    private final Set<String> matches;

    public SolrDocumentToOrganisaatioPerustietoTypeFunction(Set<String> matches) {
        this.matches = matches;
    }

    @Override
    public OrganisaatioPerustieto apply(SolrDocument doc) {
        Preconditions.checkNotNull(doc);
        OrganisaatioPerustieto result = new OrganisaatioPerustieto();
        setNimiIfNotNull("en", NIMIEN, doc, result);
        setNimiIfNotNull("fi", NIMIFI, doc, result);
        setNimiIfNotNull("sv", NIMISV, doc, result);
        result.setOid(sGet(doc, OID));
        result.setParentOid(sGet(doc, PARENTOID));
        result.setOppilaitosKoodi(sGet(doc, OPPILAITOSKOODI));
        if(matches==null || matches.contains(result.getOid())) {
            result.setMatch(true);
        }
        Collection<Object> values = doc.getFieldValues(ORGANISAATIOTYYPPI);
        if (values != null) {
            for (Object value : values) {
                result.getOrganisaatiotyypit().add(
                        OrganisaatioTyyppi.fromValue((String) value));
            }
        }
        values = doc.getFieldValues(KIELI);
        if (values != null) {
            for (Object value : values) {
                result.getKieletUris().add((String) value);
            }
        }
        result.setKotipaikkaUri(sGet(doc, KUNTA));

        if (doc.getFirstValue(PATH) != null) {
            result.setParentOidPath(Joiner.on("/").join(
                    doc.getFieldValues(PATH)));
        }

        result.setYtunnus(sGet(doc, YTUNNUS));
        result.setAlkuPvm(dGet(doc, ALKUPVM));
        result.setLakkautusPvm(dGet(doc, LAKKAUTUSPVM));
        result.setOppilaitostyyppi(sGet(doc, OPPILAITOSTYYPPI));
        result.setToimipisteKoodi(sGet(doc, TOIMIPISTEKOODI));
        result.setAliOrganisaatioMaara(iGet(doc, ALIORGANISAATIOIDEN_LKM));

        return result;
    }

    private void setNimiIfNotNull(String targetLanguage, String sourceField, SolrDocument doc,
            OrganisaatioPerustieto result) {
        final String nimi = sGet(doc, sourceField);
        if(nimi!=null) {
            result.setNimi(targetLanguage, nimi);
        }
    }

    /**
     * Get date value
     * 
     * @param doc
     * @param field
     * @return
     */
    private Date dGet(SolrDocument doc, String field) {
        return (Date) doc.getFieldValue(field);
    }

    /**
     * Get String value
     * 
     * @param doc
     * @param field
     * @return
     */
    private String sGet(SolrDocument doc, String field) {
        return (String) doc.getFieldValue(field);
    }

    private int iGet(SolrDocument doc, String field) {
        Integer ret = (Integer) doc.getFieldValue(field);
        return ret == null ? 0 : ret;
    }

}
