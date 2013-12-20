/*
 *
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
package fi.vm.sade.organisaatio.revised.ui.helper;

import fi.vm.sade.generic.common.I18N;
import fi.vm.sade.koodisto.service.types.SearchKoodisCriteriaType;
import fi.vm.sade.koodisto.service.types.common.KoodiType;
import fi.vm.sade.koodisto.util.CachingKoodistoClient;
import fi.vm.sade.koodisto.util.KoodiServiceSearchCriteriaBuilder;
import fi.vm.sade.koodisto.widget.KoodistoComponent;
import net.sf.ehcache.CacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Tuomas Katva
 */
@Configurable(preConstruction = true)
public class KoodistoHelper {

    private static final Logger LOG = LoggerFactory.getLogger(KoodistoHelper.class);

    private CachingKoodistoClient koodistoRestClient = new CachingKoodistoClient();

    public static final String UriVersioSplitter = "#";

    @Autowired
    @Qualifier(value = "ehcacheOrganisaatio")
    private CacheManager _cacheManager;

    private static long _nextCacheStatsPrinted = 0L;

    /**
     * Print cache stats for cache.
     * ... the scheduling just didn't work here :(
     */
    public void printCacheStats() {
        if (_nextCacheStatsPrinted < System.currentTimeMillis()) {
            _nextCacheStatsPrinted = System.currentTimeMillis() + 1000L * 600L;

            LOG.info("*** printCacheStats()");
            for (String cacheName : _cacheManager.getCacheNames()) {
                LOG.info("*** {}", _cacheManager.getCache(cacheName).getStatistics());
            }
        }
    }

    String tryGetKoodistoArvo(String arvo, String koodistoUri) {
        printCacheStats();
        try {
            /*
            SearchKoodistosCriteriaType koodistoSearchCriteria = KoodistoServiceSearchCriteriaBuilder.latestKoodistoByUri(koodistoUri);

            List<KoodistoType> koodistoResult = getKoodistoService().searchKoodistos(koodistoSearchCriteria);
            if(koodistoResult.size() != 1) {
                // FIXME: Throw something other than RuntimeException?
                throw new RuntimeException("No koodisto found for koodisto URI " + koodistoUri);
            }
            KoodistoType koodisto = koodistoResult.get(0);

            SearchKoodisByKoodistoCriteriaType koodiSearchCriteria = KoodiServiceSearchCriteriaBuilder.koodisByArvoAndKoodistoUriAndKoodistoVersio(arvo,
                    koodistoUri, koodisto.getVersio());

            List<KoodiType> koodit = getKoodiService().searchKoodisByKoodisto(koodiSearchCriteria);
            if (koodit != null && koodit.size() > 0) {
                return koodit.get(0).getKoodiUri();
            } else {
                return arvo;
            }
            */
            List<KoodiType> koodit = koodistoRestClient.getKoodisForKoodisto(koodistoUri, null); // vois olla omakin rest tätä varten mutta kakutuskin toimii
            for (KoodiType koodi : koodit) {
                if (arvo.equals(koodi.getKoodiArvo())) {
                    return koodi.getKoodiUri();
                }
            }
            return arvo;

        } catch (Exception exp) {
            return arvo;
        }
    }

    public String tryGetArvoByKoodi(String koodi) {
        printCacheStats();
        if (koodi == null || koodi.isEmpty()) {
            return "";
        }
        try {
            SearchKoodisCriteriaType searchCriteria = null;
            if (koodi.contains(UriVersioSplitter) && koodi.split(UriVersioSplitter).length > 1) {
                String[] uriAndVersio = koodi.split(UriVersioSplitter);
                int versio = Integer.parseInt(uriAndVersio[1]);
                searchCriteria = KoodiServiceSearchCriteriaBuilder.koodiByUriAndVersion(uriAndVersio[0], versio);
            } else {
                searchCriteria = KoodiServiceSearchCriteriaBuilder.latestKoodisByUris(koodi);//latestAcceptedKoodiByUri(koodi);//latestValidAcceptedKoodiByUri(koodi);
            }
            List<KoodiType> result = koodistoRestClient.searchKoodis(searchCriteria);
           if(result.size() != 1) {
               // FIXME: Throw something other than RuntimeException?
               throw new RuntimeException("No koodi found for koodi URI " + koodi);
           }

        KoodiType koodidto = result.get(0);
        return fi.vm.sade.koodisto.util.KoodistoHelper.getKoodiMetadataForLanguage(koodidto, fi.vm.sade.koodisto.util.KoodistoHelper.getKieliForLocale(I18N.getLocale())).getNimi();
        } catch (Exception exp) {
            return koodi;
        }
    }

    /**
     * @return the koodiService
     */
    /*
    public KoodiService getKoodiService() {
        return koodiService;
    }
    */

    /**
     * @param koodiService the koodiService to set
     */
    /*
    public void setKoodiService(KoodiService koodiService) {
        this.koodiService = koodiService;
    }
    */

    /**
     * @return the koodistoService
     */
    /*
    public KoodistoService getKoodistoService() {
        return koodistoService;
    }
    */

    /**
     * @param koodistoService the koodistoService to set
     */
    /*
    public void setKoodistoService(KoodistoService koodistoService) {
        this.koodistoService = koodistoService;
    }
    */

    /**
     * Filters out koodis that the end date of which is passed from a koodisto component.
     * @param koodistoC - the koodisto component
     * @param koodistoUri - the koodisto uri of the koodisto component
     */
    public void filterOutOldKoodit(KoodistoComponent koodistoC, String koodistoUri) { // TODO: ennemmin parametrisoisi KoodistoComponentin niin, että sille voi kertoa, haetaanko kaikki koodit vai vain validit, koska koodiston rest apikin tukee tätä
        List<String> kooditToRemove = new ArrayList<String>();
        /*
        SearchKoodisByKoodistoCriteriaType sCriteria = new SearchKoodisByKoodistoCriteriaType();
        sCriteria.setKoodistoUri(koodistoUri);
        List<KoodiType> koodis = this.koodiService.searchKoodisByKoodisto(sCriteria);
        */
        List<KoodiType> koodis = koodistoRestClient.getKoodisForKoodisto(koodistoUri, null);
        for (KoodiType curKoodi : koodis) {
            if ((curKoodi != null)
                    && (curKoodi.getVoimassaLoppuPvm() != null)
                    && curKoodi.getVoimassaLoppuPvm().toGregorianCalendar().getTime().before(new Date())) {
                kooditToRemove.add(curKoodi.getKoodiUri());
            }
        }
        for (String curKoodi : kooditToRemove) {
            koodistoC.getField().removeItem(curKoodi);
        }
    }


}
