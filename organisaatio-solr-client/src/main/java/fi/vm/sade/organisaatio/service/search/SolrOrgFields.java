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

/**
 * Solr index field names.
 */
public class SolrOrgFields {
    protected static final String NIMIEN = "nimien_t";
    protected static final String NIMIFI = "nimifi_t";
    protected static final String NIMISV = "nimisv_t";
    protected static final String NIMISEARCH = "nimihaku_tnws";
    protected static final String PATH = "path_ss";
    protected static final String KUNTA = "kunta_t";
    protected static final String DOMAINNIMI = "domain_t";
    protected static final String ORGANISAATIOTYYPPI = "organisaatiotyypi_ss";
    protected static final String KIELI = "kieli_ss";

    protected static final String OID = "id";
    protected static final String PARENTOID = "parentoid_s";
    protected static final String OPPILAITOSKOODI = "oppilaitoskoodi_s";
    protected static final String OPPILAITOSTYYPPI = "oppilaitostyyppi_s";
    protected static final String TOIMIPISTEKOODI = "toimipistekoodi_s";
    protected static final String YTUNNUS = "ytunnus_s";
    protected static final String ALKUPVM = "alkupvm_dt";
    protected static final String LAKKAUTUSPVM = "lakkautuspvm_dt";
    protected static final String ALIORGANISAATIOIDEN_LKM = "aliorganisaatioiden_lkm_i";
}
