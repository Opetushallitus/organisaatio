/*
 * Copyright (c) 2014 The Finnish Board of Education - Opetushallitus
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
package fi.vm.sade.organisaatio.business.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fi.vm.sade.generic.model.BaseEntity;
import fi.vm.sade.generic.rest.CachingRestClient;
import fi.vm.sade.organisaatio.model.Organisaatio;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Päivittää organisaatiopalvelussa lisätyn tai muokatun organisaation tiedot
 * koodistoon.
 *
 */
@Component
public class OrganisaatioKoodisto {

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    // NOTE! cachingRestClient is static because we need application-scoped rest cache for organisaatio-service
    private static final CachingRestClient cachingRestClient = new CachingRestClient();

    @Value("${organisaatio-service.koodisto-service.rest.url}")
    private String koodistoServiceWebappUrl;

    private Gson gson;

    /**
     * Luo instanssin ja alustaa gson:in
     */
    public OrganisaatioKoodisto() {
        gson = new GsonBuilder().create();
    }

    /**
     * Hakee koodin koodistosta
     *
     * @param koodistoUri Koodisto URI
     * @param tunniste URI-spesifinen tunniste (toimipistekoodi,
     * oppilaitosnumero tai y-tunnus ilman väliviivaa)
     * @return Koodiobjekti tai null jos ei löytynyt
     */
    private Koodi haeKoodi(String koodistoUri, String tunniste) {
        Koodi res;
        try {
            long t0 = System.currentTimeMillis();
            res = cachingRestClient.get(koodistoServiceWebappUrl + "/json/" + koodistoUri + "/koodi/" + koodistoUri + "_" + tunniste, Koodi.class);
            LOG.debug("koodisto rest get done, koodistoUri: {}, tunniste: {}, took: {} ms, cacheStatus: {}",
                    new Object[]{koodistoUri, tunniste, (System.currentTimeMillis() - t0), cachingRestClient.getCacheStatus()});
        } catch (IOException e) {
            LOG.debug("Koodi not found.");
            return null;
        }
        LOG.debug(res.toString());
        return res;
    }

    // Päivittää olemassaolevan koodin kutsumalla cachingRestClientin PUT-operaatiota
    private boolean paivitaKoodi(Koodi koodi) {
        String json = gson.toJson(koodi);
        LOG.debug("Päivitetään koodi: " + json);

        try {
            HttpResponse res = cachingRestClient.put(koodistoServiceWebappUrl + "/codeelement", "application/json;charset=utf-8", json);
        } catch (IOException e) {
            LOG.warn("Koodi update failed. Reason: " + e.getMessage());
            return false;
        }
        return true;
    }

    // Lisää uuden koodin kutsumalla cachingRestClientin POST-operaatiota
    private boolean lisaaKoodi(Koodi koodi, String uri) {
        String json = gson.toJson(koodi);
        LOG.debug("Lisätään koodi: " + json);

        try {
            HttpResponse res = cachingRestClient.post(koodistoServiceWebappUrl + "/codeelement/" + uri, "application/json;charset=utf-8", json);
        } catch (IOException e) {
            LOG.warn("Koodi insert failed. Reason: " + e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Lisää koodistoon uuden koodin koodiarvolla <uri>_<tunniste>
     *
     * @param uri KoodiURI
     * @param tunniste Uri-kohtainen tunniste
     * @param entity Organisaatio jolle koodi lisätään
     * @return True jos onnistui, false jos lisääminen ei onnistunut
     */
    private boolean luoKoodi(String uri, String tunniste, Organisaatio entity) {
        // Lisätään koodi
        Koodi uk = new Koodi();

        uk.setKoodiArvo(uri + "_" + tunniste);
        uk.setVoimassaAlkuPvm(new SimpleDateFormat("yyyy-MM-dd").format(entity.getAlkuPvm()));
        uk.setVersio(1);

        for (String lang : entity.getNimi().getValues().keySet()) {
            KoodiMetadata umt = new KoodiMetadata();
            umt.setKieli(lang.toUpperCase());
            umt.setNimi(entity.getNimi().getString(lang));
            umt.setLyhytNimi(entity.getNimi().getString(lang));
            umt.setKuvaus(entity.getNimi().getString(lang));
            uk.getMetadata().add(umt);
        }
        return lisaaKoodi(uk, uri);
    }

    /**
     * Päivittää koodiston vastaamaan muokattua organisaatiota. Jos
     * organisaation nimeä tai lakkautuspäivämäärää on muutettu, koodistoon
     * päivitetään muuttuneet tiedot. Jos organisaatio on uusi ja koodia ei
     * löydy, luodaan uusi koodi. Päivitettävä koodi riippuu parametrina annetun
     * organisaation tyypistä seuraavasti: - Toimipiste (organisaatiolla on
     * toimipistekoodi muttei oppilaitoskoodia) => Päivitetään koodi
     * opetuspisteet_<toimipistekoodi>
     * - Oppilaitos (organisaatiolla on oppilaitoskoodi) => Päivitetään koodi
     * oppilaitosnumero_<oppilaitoskoodi>
     * - Koulutustoimija (organisaatiolla on y-tunnus) => Päivitetään koodi
     * koulutustoimija_<y-tunnus>
     * - Jos organisaatiolla on yhteishaunkoulukoodi => Päivitetään koodi
     * yhteishaunkoulukoodi_<yhteishaunkoulukoodi>
     *
     * @param entity Organisaatio
     * @return true jos koodiston päivittäminen onnistui, false muuten
     */
    public boolean paivitaKoodisto(Organisaatio entity) {
        /*
         * Koodiston koodit
         *     [0]: koodiUri
         *     [1]: tunniste
         */
        Object[][] koodiLista = {
            new Object[]{"opetuspisteet",
                (entity.getOppilaitosKoodi() != null && !entity.getOppilaitosKoodi().isEmpty()) ? null : entity.getToimipisteKoodi()},
            new Object[]{"oppilaitosnumero", entity.getOppilaitosKoodi()},
            new Object[]{"koulutustoimija", entity.getYtunnus()},
            new Object[]{"yhteishaunkoulukoodi", entity.getYhteishaunKoulukoodi()}
        };
        int URI_INDEX = 0;
        int TUNNISTE_INDEX = 1;

        for (Object[] koodiAlkio : koodiLista) {
            String uri = (String) koodiAlkio[URI_INDEX];
            String tunniste = (String) koodiAlkio[TUNNISTE_INDEX];
            Koodi koodi = null;
            LOG.debug("KOODI uri: " + uri + ", tunniste: '" + tunniste + "'");
            if (tunniste != null && !tunniste.isEmpty()) {
                // Tunniste on olemassa, haetaan koodistosta
                koodi = haeKoodi(uri, tunniste);

                if (koodi == null) {
                    // Ei löytynyt, luodaan uusi
                    if (luoKoodi(uri, tunniste, entity) == true) {
                        LOG.debug("Koodi " + uri + "_" + tunniste + " lisätty");
                    } else {
                        LOG.warn("Koodin " + uri + "_" + tunniste + " lisääminen epäonnistui");
                        return false;
                    }
                } else {
                    // Päivitetään olemassa olevaan nimi ja lakkautuspvm jos muuttuneet
                    boolean muuttunut = false;

                    // Tarkistetaan onko lakkautuspäivämäärä muuttunut
                    if (entity.getLakkautusPvm() != null) {
                        String loppupvm = new SimpleDateFormat("yyyy-MM-dd").format(entity.getLakkautusPvm());
                        if (!loppupvm.equals(koodi.getVoimassaLoppuPvm())) {
                            LOG.debug("Lakkautuspvm muuttunut");
                            koodi.setVoimassaLoppuPvm(loppupvm);
                            muuttunut = true;
                        }
                        // TODO: tarkista onko nimi muuttunut
                    } else if (koodi.getVoimassaLoppuPvm() != null && !koodi.getVoimassaLoppuPvm().isEmpty()) {
                        LOG.debug("Lakkautuspvm poistettu");
                        koodi.setVoimassaLoppuPvm(null);
                        muuttunut = true;
                    }

                    // Koodin nimet tällä hetkellä koodistossa
                    Map<String, KoodiMetadata> koodiNyt = new HashMap<String, KoodiMetadata>();
                    for (KoodiMetadata km : koodi.getMetadata()) {
                        koodiNyt.put(km.getKieli(), km);
                    }

                    // Tarkistetaan onko nimi muuttunut
                    for (String kieli : entity.getNimi().getValues().keySet()) {
                        String uusiNimi = entity.getNimi().getValues().get(kieli);
                        LOG.debug("Nimi: " + kieli.toUpperCase() + "=" + uusiNimi);

                        KoodiMetadata km = koodiNyt.get(kieli.toUpperCase());
                        if (km != null) {
                            // Kieliversio on olemassa
                            if (!km.getNimi().equals(uusiNimi)) {
                                LOG.debug("Nimi(" + kieli + ") muuttunut");
                                km.setNimi(uusiNimi);
                                // TODO: lyhytnimi, kuvaus?
                                muuttunut = true;
                            } else {
                                LOG.debug("Nimi(" + kieli + ") ei ole muuttunut");
                            }
                        } else {
                            // Lisää koodiin
                            KoodiMetadata ukm = new KoodiMetadata();
                            ukm.setKieli(kieli.toUpperCase());
                            ukm.setNimi(uusiNimi);
                            //ukm.setLyhytNimi(uusiNimi);
                            //ukm.setKuvaus(uusiNimi);
                            koodi.getMetadata().add(ukm);
                            LOG.debug("Nimi(" + kieli + ") lisätty");
                            muuttunut = true;
                        }
                    }

                    if (muuttunut == true) {
                        paivitaKoodi(koodi);
                    } else {
                        LOG.debug("Ei muutoksia");
                    }
                }
            }
        }
        return true;
    }

    /**
     * Koodi-luokka, jonka gson serialisoi/unserialisoi REST-kutsujen JSON:sta.
     *
     * Sisältää vain tämän toiminnalisuuden ja rajapintojen käytön kannalta tarpeelliset propertyt.
     *
     */
    public class Koodi {

        private String koodiUri;

        //Ei tarvita tässä yhteydessä
        //private String resourceUri;
        private Long version;

        private int versio;

        private String koodiArvo;

        protected String paivitysPvm;

        protected String voimassaAlkuPvm;

        protected String voimassaLoppuPvm;

        protected List<KoodiMetadata> metadata = new ArrayList<KoodiMetadata>();

        public String getKoodiUri() {
            return koodiUri;
        }

        public void setKoodiUri(String koodiUri) {
            this.koodiUri = koodiUri;
        }

        /*public String getResourceUri() {
         return resourceUri;
         }

         public void setResourceUri(String resourceUri) {
         this.resourceUri = resourceUri;
         }*/
        public int getVersio() {
            return versio;
        }

        public void setVersio(int versio) {
            this.versio = versio;
        }

        public String getKoodiArvo() {
            return koodiArvo;
        }

        public void setKoodiArvo(String koodiArvo) {
            this.koodiArvo = koodiArvo;
        }

        public String getPaivitysPvm() {
            return paivitysPvm;
        }

        public void setPaivitysPvm(String paivitysPvm) {
            this.paivitysPvm = paivitysPvm;
        }

        public String getVoimassaAlkuPvm() {
            return voimassaAlkuPvm;
        }

        public void setVoimassaAlkuPvm(String voimassaAlkuPvm) {
            this.voimassaAlkuPvm = voimassaAlkuPvm;
        }

        public String getVoimassaLoppuPvm() {
            return voimassaLoppuPvm;
        }

        public void setVoimassaLoppuPvm(String voimassaLoppuPvm) {
            this.voimassaLoppuPvm = voimassaLoppuPvm;
        }

        public List<KoodiMetadata> getMetadata() {
            return metadata;
        }

        public void setMetadata(List<KoodiMetadata> metadata) {
            this.metadata = metadata;
        }

        public Long getVersion() {
            return version;
        }

        public void setVersion(final Long version) {
            this.version = version;
        }
    }

    /**
     * KoodiMetadata-luokka on osa Koodi:a jonka gson serialisoi/unserialisoi REST-kutsujen JSON:sta.
     */
    public class KoodiMetadata extends BaseEntity {

        private String nimi;

        private String kuvaus;

        private String lyhytNimi;

        private String kayttoohje;

        private String kasite;

        private String sisaltaaMerkityksen;

        private String eiSisallaMerkitysta;

        private String huomioitavaKoodi;

        private String sisaltaaKoodiston;

        private String kieli;

        public String getNimi() {
            return nimi;
        }

        public void setNimi(String nimi) {
            this.nimi = nimi;
        }

        public String getKuvaus() {
            return kuvaus;
        }

        public void setKuvaus(String kuvaus) {
            this.kuvaus = kuvaus;
        }

        public String getLyhytNimi() {
            return lyhytNimi;
        }

        public void setLyhytNimi(String lyhytNimi) {
            this.lyhytNimi = lyhytNimi;
        }

        public String getKayttoohje() {
            return kayttoohje;
        }

        public void setKayttoohje(String kayttoohje) {
            this.kayttoohje = kayttoohje;
        }

        public String getKasite() {
            return kasite;
        }

        public void setKasite(String kasite) {
            this.kasite = kasite;
        }

        public String getSisaltaaMerkityksen() {
            return sisaltaaMerkityksen;
        }

        public void setSisaltaaMerkityksen(String sisaltaaMerkityksen) {
            this.sisaltaaMerkityksen = sisaltaaMerkityksen;
        }

        public String getEiSisallaMerkitysta() {
            return eiSisallaMerkitysta;
        }

        public void setEiSisallaMerkitysta(String eiSisallaMerkitysta) {
            this.eiSisallaMerkitysta = eiSisallaMerkitysta;
        }

        public String getHuomioitavaKoodi() {
            return huomioitavaKoodi;
        }

        public void setHuomioitavaKoodi(String huomioitavaKoodi) {
            this.huomioitavaKoodi = huomioitavaKoodi;
        }

        public String getSisaltaaKoodiston() {
            return sisaltaaKoodiston;
        }

        public void setSisaltaaKoodiston(String sisaltaaKoodiston) {
            this.sisaltaaKoodiston = sisaltaaKoodiston;
        }

        public String getKieli() {
            return kieli;
        }

        public void setKieli(String kieli) {
            this.kieli = kieli;
        }

    }
}
